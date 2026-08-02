package com.k9x.domain.dogs.rank;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * The competitor index of {@code K9X_indice_nivel_spec.md}: {@code index = level × freshness}, computed over a
 * dog's full rank history.
 *
 * <p><b>Level</b> (§3.1/§4) is the weighted mean of every recorded score, each weighted by its age on the level
 * curve — full weight for 10 months, then a linear ramp through fixed anchors down to a permanent 0.01 floor at
 * month 70. It measures the sustained level the dog has demonstrated: a single outlier (a bad day, a lucky day)
 * is diluted by the rest of the history, and old results fade gradually, never abruptly.
 *
 * <p><b>Freshness</b> (§3.2) is a second curve — same shape, steeper on the 10–18 month stretch — evaluated on
 * the age of the <em>most recent</em> result only. It scales the whole index down while the dog is inactive and
 * snaps back to 1.0 as soon as it competes again (recoverable, unlike a subtractive penalty). The 0.01 floor on
 * both curves guarantees no dog ever disappears from the ranking.
 */
public final class DogRankIndex {

    private static final double PLATEAU_MONTHS = 10.0;
    private static final double FLOOR = 0.01;
    private static final double DAYS_PER_MONTH = 30.4375;
    private static final double MILLIS_PER_DAY = 86_400_000.0;
    private static final int LEVEL_SCALE = 4;

    private static final double[][] LEVEL_ANCHORS = {
            {10, 1.00}, {22, 0.75}, {34, 0.50}, {46, 0.25}, {58, 0.10}, {70, 0.01}
    };
    private static final double[][] FRESHNESS_ANCHORS = {
            {10, 1.00}, {12, 0.92}, {18, 0.84}, {22, 0.75},
            {34, 0.50}, {46, 0.25}, {58, 0.10}, {70, 0.01}
    };

    /** One recorded result of the dog's rank history: the raw score and when it was earned. */
    public record Result(BigDecimal score, long timestamp) {
    }

    private DogRankIndex() {
    }

    /**
     * The dog's index over its full history, rounded HALF_UP to an integer (the 0–1000 {@code dogs.rank}
     * scale): the level-weighted mean of every score, scaled by the freshness of the most recent one.
     *
     * @param results the dog's rank history, at least one result.
     * @param now     current epoch millis.
     */
    public static int of(List<Result> results, long now) {
        BigDecimal numerator = BigDecimal.ZERO;
        BigDecimal denominator = BigDecimal.ZERO;
        double mostRecentMonths = Double.MAX_VALUE;

        for (Result result : results) {
            double months = monthsBetween(result.timestamp(), now);
            BigDecimal weight = BigDecimal.valueOf(levelWeight(months));
            numerator = numerator.add(weight.multiply(result.score()));
            denominator = denominator.add(weight);
            mostRecentMonths = Math.min(mostRecentMonths, months);
        }

        BigDecimal level = numerator.divide(denominator, LEVEL_SCALE, RoundingMode.HALF_UP);
        BigDecimal freshness = BigDecimal.valueOf(freshness(mostRecentMonths));
        return level.multiply(freshness).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    /** The level-curve weight (§3.1) for a result that is {@code months} old: plateau, anchored ramp, floor. */
    public static double levelWeight(double months) {
        return interpolate(months, LEVEL_ANCHORS);
    }

    /** The freshness factor (§3.2) for a most-recent result that is {@code months} old. */
    public static double freshness(double months) {
        return interpolate(months, FRESHNESS_ANCHORS);
    }

    private static double interpolate(double months, double[][] anchors) {
        if (months <= PLATEAU_MONTHS) {
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

    private static double monthsBetween(long from, long to) {
        return Math.max(0, to - from) / MILLIS_PER_DAY / DAYS_PER_MONTH;
    }
}
