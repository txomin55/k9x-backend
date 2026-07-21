package com.k9x.domain.disciplines.obdx;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * The text qualification (calificativo) a competitor earns from its total score against the configuration's
 * qualification scale. Federations that don't use a scale leave the field absent.
 */
public final class ObdxQualification {

    /** Fallback qualification when the total score does not reach the lowest configured tier. */
    public static final String NOT_CLASSIFIED = "NC";
    /** Qualification for a competitor that is disqualified (red card / second yellow) or not competing. */
    public static final String DISQUALIFIED = "DISQ";

    private ObdxQualification() {
    }

    /**
     * A qualification tier keyed by an i18n-resolvable id (e.g. {@code OBDX_QUALIFICATION_EXC}) and the minimum
     * absolute total score required to reach it.
     */
    public record Tier(String id, BigDecimal minScore) {
    }

    /**
     * Resolves the qualification, in this order, independently of the numeric total for the first two cases:
     * <ol>
     *     <li>a disqualified or not-competing competitor is {@link #DISQUALIFIED} (DISQ);</li>
     *     <li>a competitor with no recorded score has no qualification yet ({@code null});</li>
     *     <li>otherwise it is the id of the highest tier whose {@code minScore} the total reaches, or
     *     {@link #NOT_CLASSIFIED} (NC) when it reaches none.</li>
     * </ol>
     * Returns {@code null} when the configuration defines no qualification scale.
     */
    public static String resolve(List<Tier> tiers, BigDecimal totalScore, boolean disqualifiedOrNotCompeting,
                                 boolean hasScore) {
        if (tiers == null || tiers.isEmpty()) {
            return null;
        }
        if (disqualifiedOrNotCompeting) {
            return DISQUALIFIED;
        }
        if (!hasScore) {
            return null;
        }
        return tiers.stream()
                .filter(t -> t.minScore() != null && totalScore.compareTo(t.minScore()) >= 0)
                .max(Comparator.comparing(Tier::minScore))
                .map(Tier::id)
                .orElse(NOT_CLASSIFIED);
    }

    /** Lowest tier threshold (the first qualification), or {@code null} when the scale is empty. */
    public static BigDecimal minThreshold(List<Tier> tiers) {
        return threshold(tiers, Comparator.naturalOrder());
    }

    /** Highest tier threshold (the top qualification / knee), or {@code null} when the scale is empty. */
    public static BigDecimal maxThreshold(List<Tier> tiers) {
        return threshold(tiers, Comparator.reverseOrder());
    }

    private static BigDecimal threshold(List<Tier> tiers, Comparator<BigDecimal> order) {
        if (tiers == null) {
            return null;
        }
        return tiers.stream()
                .map(Tier::minScore)
                .filter(Objects::nonNull)
                .min(order)
                .orElse(null);
    }
}
