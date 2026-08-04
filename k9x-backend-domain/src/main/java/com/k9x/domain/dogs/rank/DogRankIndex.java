package com.k9x.domain.dogs.rank;

import com.k9x.domain.disciplines.obdx.ObdxConfigurationsRankThresholds;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * The competitor index of {@code K9X_indice_nivel_spec.md}: {@code index = level × freshness}, computed over a
 * dog's full rank history.
 *
 * <p><b>Level</b> (§4.1) is the sum of the dog's {@value #SLOTS} best <em>contributions</em> — each result's
 * score weighted by its age on the level curve — divided by a <b>fixed</b> denominator of {@value #SLOTS}. Two
 * properties follow from that fixed denominator, and both are load-bearing:
 *
 * <ul>
 *   <li><b>Competing never lowers the index.</b> A result below the dog's level does not displace a better one,
 *       so it cannot subtract; only time can. A dog that wins a world final may enter minor trials without
 *       damaging its rank.</li>
 *   <li><b>One exceptional result does not define a dog.</b> A single score contributes at most
 *       {@code 1/}{@value #SLOTS} of the level, so a 950 must be repeated to read as a 950.</li>
 * </ul>
 *
 * <p>Slots the dog has not filled yet are worth {@link #PRIOR} capped by its own best contribution (§4.1.1):
 * the prior is a <em>ceiling on what is assumed</em>, never a floor, so a dog whose only result is a 200 reads
 * 200 rather than being lifted to the prior.
 *
 * <p><b>Freshness</b> (§3.2) is a second, steeper curve evaluated on the age of the <em>most recent</em> result
 * only, whatever that result scored. It scales the whole index down while the dog is inactive and snaps back to
 * 1.0 as soon as it competes again (recoverable, unlike a subtractive penalty), so entering a minor trial always
 * pays off even when it adds nothing to the level.
 *
 * <p>Both curves are calibrated to a real canine career — dogs start competing at 2-3 years old and retire at
 * 8-10, so 60-96 months is a whole career (§3.0): the level plateau (8 months) covers the time it takes to fill
 * the {@value #SLOTS} slots, and the freshness plateau ({@value #FRESHNESS_PLATEAU_MONTHS_THRESHOLD} months) the
 * normal gap between trials. The 0.01 floor on both curves guarantees no dog ever disappears from the ranking.
 */
public final class DogRankIndex {

    /** Level slots and, at the same time, the fixed denominator of the level (§4.1). */
    public static final int SLOTS = 3;

    /**
     * Freshness plateau: while the most recent result is younger than this an inactive dog does not degrade at
     * all, so the history cron has nothing to record.
     */
    public static final int FRESHNESS_PLATEAU_MONTHS_THRESHOLD = 6;

    /**
     * Month where the freshness curve reaches its permanent 0.01 floor. Past it an inactive dog's index stops
     * moving — freshness is the curve that keeps degrading an inactive dog, so this is the month the history
     * cron stops appending degradation records at.
     */
    public static final int FRESHNESS_FLOOR_MONTHS_THRESHOLD = 58;

    /**
     * The prior C that fills empty level slots (§4.1.1): the floor of the first competitive band on the shared
     * 0-1000 rank scale, i.e. what is assumed of a competitor nothing is known about yet. Every discipline
     * shares that scale, so a single value covers them all; the day a discipline needs its own it becomes a
     * parameter of {@link #of(List, long)}.
     */
    public static final BigDecimal PRIOR = BigDecimal.valueOf(ObdxConfigurationsRankThresholds.FCI_GRADE_1.min());

    /** Level plateau: results younger than this many months carry no degradation at all. */
    private static final int LEVEL_PLATEAU_MONTHS = 8;

    private static final double FLOOR = 0.01;
    private static final double DAYS_PER_MONTH = 30.4375;
    private static final double MILLIS_PER_DAY = 86_400_000.0;
    private static final int LEVEL_SCALE = 4;

    private static final double[][] LEVEL_ANCHORS = {
            {8, 1.00}, {14, 0.85}, {20, 0.65}, {26, 0.45}, {32, 0.25}, {44, 0.05}, {56, 0.01}
    };
    private static final double[][] FRESHNESS_ANCHORS = {
            {6, 1.00}, {10, 0.80}, {16, 0.60}, {22, 0.40},
            {28, 0.25}, {34, 0.12}, {46, 0.03}, {58, 0.01}
    };

    /** One recorded result of the dog's rank history: the raw score and when it was earned. */
    public record Result(BigDecimal score, long timestamp) {
    }

    private DogRankIndex() {
    }

    /**
     * The dog's index over its full history, rounded HALF_UP to an integer (the 0-1000 {@code dogs.rank}
     * scale): its {@value #SLOTS} best age-weighted contributions over a fixed denominator, scaled by the
     * freshness of the most recent result.
     *
     * @param results the dog's rank history, at least one result.
     * @param now     current epoch millis.
     */
    public static int of(List<Result> results, long now) {
        // Each result's contribution: age weight × score, best first.
        List<BigDecimal> contributions = results.stream()
                .map(result -> BigDecimal.valueOf(levelWeight(monthsBetween(result.timestamp(), now)))
                        .multiply(result.score()))
                .sorted(Comparator.reverseOrder())
                .toList();

        // Empty slots are worth min(PRIOR, own best contribution): a ceiling on what is assumed, never a floor.
        BigDecimal emptySlot = PRIOR.min(contributions.get(0));

        // Level: the best SLOTS among real contributions and empty slots, over the fixed denominator SLOTS.
        BigDecimal level = Stream.concat(contributions.stream(), Stream.generate(() -> emptySlot).limit(SLOTS))
                .sorted(Comparator.reverseOrder())
                .limit(SLOTS)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(SLOTS), LEVEL_SCALE, RoundingMode.HALF_UP);

        double mostRecentMonths = results.stream()
                .mapToDouble(result -> monthsBetween(result.timestamp(), now))
                .min()
                .orElseThrow();

        int index = level.multiply(BigDecimal.valueOf(freshness(mostRecentMonths)))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        // No dog that has ever competed drops to 0: both curves floor at 0.01, so a decades-old history would
        // otherwise round down to nothing and tie every retired dog together.
        return Math.max(1, index);
    }

    /** The level-curve weight (§3.1) for a result that is {@code months} old: plateau, anchored ramp, floor. */
    public static double levelWeight(double months) {
        return interpolate(months, LEVEL_PLATEAU_MONTHS, LEVEL_ANCHORS);
    }

    /** The freshness factor (§3.2) for a most-recent result that is {@code months} old. */
    public static double freshness(double months) {
        return interpolate(months, FRESHNESS_PLATEAU_MONTHS_THRESHOLD, FRESHNESS_ANCHORS);
    }

    private static double interpolate(double months, double plateauMonths, double[][] anchors) {
        if (months <= plateauMonths) {
            return 1.0;
        }
        if (months >= anchors[anchors.length - 1][0]) {
            return FLOOR;
        }
        for (int i = 0; i < anchors.length - 1; i++) {
            double m0 = anchors[i][0], w0 = anchors[i][1];
            double m1 = anchors[i + 1][0], w1 = anchors[i + 1][1];
            if (months >= m0 && months <= m1) {
                double t = (months - m0) / (m1 - m0);
                return w0 + t * (w1 - w0);
            }
        }
        return FLOOR;
    }

    /** Whole months elapsed between two instants (floor of the fractional month count, never negative). */
    public static int wholeMonthsBetween(long from, long to) {
        return (int) monthsBetween(from, to);
    }

    private static double monthsBetween(long from, long to) {
        return Math.max(0, to - from) / MILLIS_PER_DAY / DAYS_PER_MONTH;
    }
}
