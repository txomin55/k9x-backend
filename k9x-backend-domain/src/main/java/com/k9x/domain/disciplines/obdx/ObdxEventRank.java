package com.k9x.domain.disciplines.obdx;

/**
 * Computes the numeric {@code rankScore} an OBDX event persists when it is created/updated. The rank
 * <em>letter</em> is not stored — it is derived from that score on read (see {@link ObdxRank}).
 *
 * <p>The score comes from the event's {@link ObdxEventCategory}, which picks a sub-band of the configuration's
 * band, and from the number of competitors, which positions the event inside it (see
 * {@link ObdxConfigurationsRankThresholds#eventScore(int, ObdxEventCategory)}); when the configuration declares
 * no band the score is {@code null}.
 */
public final class ObdxEventRank {

    private ObdxEventRank() {
    }

    /**
     * Whether the configuration may be run under this category — {@code false} for an unknown configuration or
     * a {@code null} category. Only the grade hosting the world championship accepts the {@code WC_*} rounds.
     */
    public static boolean isCategoryAllowed(String configurationId, ObdxEventCategory category) {
        ObdxConfigurationsRankThresholds band = ObdxConfigurationsRankThresholds.fromConfigurationId(configurationId);
        return band != null && band.allows(category);
    }

    /**
     * The event's automatic rank score for its category's sub-band, or {@code null} when the configuration
     * declares no band.
     *
     * @throws IllegalArgumentException when the category is not allowed for the configuration; callers check
     *                                  {@link #isCategoryAllowed(String, ObdxEventCategory)} first.
     */
    public static Integer eventScore(String configurationId, int competitorCount, ObdxEventCategory category) {
        ObdxConfigurationsRankThresholds band = ObdxConfigurationsRankThresholds.fromConfigurationId(configurationId);
        return band == null ? null : band.eventScore(competitorCount, category);
    }
}
