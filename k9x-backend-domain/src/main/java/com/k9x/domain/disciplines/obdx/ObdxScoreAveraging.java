package com.k9x.domain.disciplines.obdx;

import com.k9x.domain.disciplines.obdx.exceptions.ObdxNotEnoughJudgesException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * How an exercise's per-judge scores collapse into a single value, per the event's {@link ObdxAvgMethod}.
 *
 * <p>{@link ObdxAvgMethod#AVG} is a plain mean. {@link ObdxAvgMethod#MID_AVG} discards the single highest and
 * single lowest score before averaging, so it needs at least {@link #MIN_JUDGES_FOR_MID_AVG} judges to be
 * meaningful — otherwise it degenerates into (near-)AVG. That threshold is the one OBDX rule that governs both
 * the write-time validation and the read-time computation.
 *
 * <p><strong>The threshold is on the event's panel, not on each exercise.</strong> A trial with four judges may
 * still split them across rings and have an exercise scored by only two of them — the world championship
 * semifinals do exactly that: the two group exercises are scored by all four, and each individual exercise by
 * the two judges of its ring. That is a four-judge event and its method is MID_AVG; asking every single
 * exercise for four assigned judges would either reject it or force {@code event_exercises.judges} to claim
 * judges that never scored there. The trim takes care of itself further down: it only applies where there
 * really are four scores, so the group exercises get trimmed and the individual ones fall back to the mean.
 */
public final class ObdxScoreAveraging {

    /** MID_AVG drops one high and one low score, so fewer than 4 judges would leave 2 or fewer scores. */
    public static final int MIN_JUDGES_FOR_MID_AVG = 4;

    private static final int SCALE = 4;

    private ObdxScoreAveraging() {
    }

    /**
     * Whether a panel of {@code panelSize} judges suffices for {@code method}; only MID_AVG imposes a minimum.
     *
     * @param panelSize judges of the <em>event</em> ({@code obdx.event_judges}), not of one exercise.
     */
    public static boolean hasEnoughJudges(ObdxAvgMethod method, int panelSize) {
        return method != ObdxAvgMethod.MID_AVG || panelSize >= MIN_JUDGES_FOR_MID_AVG;
    }

    /**
     * Collapses the given scores into a single value. Under MID_AVG the single highest and lowest are trimmed
     * once there are at least {@value #MIN_JUDGES_FOR_MID_AVG} scores; an empty list yields {@link BigDecimal#ZERO}.
     *
     * @param panelSize judges of the <em>event</em>, not of this exercise: an exercise scored by the two judges
     *                  of its ring is normal in a four-judge trial, and it still trims wherever four judges did
     *                  score. MID_AVG on a panel of fewer than {@value #MIN_JUDGES_FOR_MID_AVG} throws.
     * @throws ObdxNotEnoughJudgesException when MID_AVG is used on too small a panel.
     */
    public static BigDecimal average(List<BigDecimal> scores, ObdxAvgMethod method, int panelSize) {
        if (scores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        if (method == ObdxAvgMethod.MID_AVG) {
            if (panelSize < MIN_JUDGES_FOR_MID_AVG) {
                throw new ObdxNotEnoughJudgesException();
            }
            if (scores.size() >= MIN_JUDGES_FOR_MID_AVG) {
                List<BigDecimal> trimmed = new ArrayList<>(scores);
                trimmed.remove(Collections.min(trimmed));
                trimmed.remove(Collections.max(trimmed));
                return mean(trimmed);
            }
        }
        return mean(scores);
    }

    /**
     * Indexes of the scores that {@link #average} excludes: under MID_AVG the single highest and single lowest
     * (a tie at an extreme excludes only one occurrence, matching {@link #average}'s removal). Everything else —
     * including every score under AVG — is kept, so the returned set is empty.
     */
    public static Set<Integer> excludedIndexes(List<BigDecimal> scores, ObdxAvgMethod method) {
        if (method != ObdxAvgMethod.MID_AVG || scores.isEmpty()) {
            return Set.of();
        }
        List<BigDecimal> working = new ArrayList<>(scores);
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++) {
            indices.add(i);
        }

        int minPos = working.indexOf(Collections.min(working));
        int minOriginalIndex = indices.remove(minPos);
        working.remove(minPos);

        if (working.isEmpty()) {
            return Set.of(minOriginalIndex);
        }

        int maxPos = working.indexOf(Collections.max(working));
        int maxOriginalIndex = indices.remove(maxPos);

        return new HashSet<>(List.of(minOriginalIndex, maxOriginalIndex));
    }

    private static BigDecimal mean(List<BigDecimal> values) {
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), SCALE, RoundingMode.HALF_UP);
    }
}
