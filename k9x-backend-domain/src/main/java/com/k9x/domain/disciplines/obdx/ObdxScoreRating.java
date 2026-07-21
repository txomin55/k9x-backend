package com.k9x.domain.disciplines.obdx;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Map;

/**
 * The OBDX scoring arithmetic that turns raw judge averages into the weighted exercise/total scores and the
 * 0–100 ratings shown in the classification, plus the flat yellow-card penalty.
 *
 * <p>Every exercise weight is its coefficient {@code coef} (default {@code 1}); a rating is always the achieved
 * value as a percentage of the maximum attainable, rounded to two decimals.
 */
public final class ObdxScoreRating {

    /** Flat points subtracted from an exercise's weighted score when the competitor was yellow-carded there. */
    public static final BigDecimal YELLOW_CARD_PENALTY = BigDecimal.TEN;

    private static final int RATIO_SCALE = 4;
    private static final int MONEY_SCALE = 2;
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private ObdxScoreRating() {
    }

    /** {@code score} as a percentage of {@code max} (0–100, two decimals); {@code 0} when {@code max} is zero. */
    public static BigDecimal percentageOfMax(BigDecimal score, BigDecimal max) {
        if (max.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return score.divide(max, RATIO_SCALE, RoundingMode.HALF_UP)
                .multiply(HUNDRED).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /** Highest attainable score for an exercise: the maximum allowed score scaled by its coefficient. */
    public static BigDecimal maxExerciseScore(BigDecimal maxAllowedScore, BigDecimal coef) {
        return maxAllowedScore.multiply(coef).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /** The competitor's weighted score for an exercise: the judges' average scaled by its coefficient. */
    public static BigDecimal weightedScore(BigDecimal average, BigDecimal coef) {
        return average.multiply(coef).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /** Applies the flat {@link #YELLOW_CARD_PENALTY} to a weighted score, never dropping below zero. */
    public static BigDecimal applyYellowCardPenalty(BigDecimal weightedScore) {
        return weightedScore.subtract(YELLOW_CARD_PENALTY).max(BigDecimal.ZERO);
    }

    /**
     * Maximum attainable total for a competitor: summed over the exercises that actually belong to the event
     * (not every exercise defined in the federation config), so it matches the numerator of the total score and
     * yields a 0–100 rating.
     */
    public static BigDecimal maxPossibleTotal(BigDecimal maxAllowedScore, Map<String, BigDecimal> coefByExerciseId,
                                              Collection<String> eventExerciseIds) {
        return eventExerciseIds.stream()
                .map(id -> maxAllowedScore.multiply(coefByExerciseId.getOrDefault(id, BigDecimal.ONE)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
