package com.k9x.domain.disciplines.obdx;

import java.util.Collection;

/**
 * Computes what an OBDX event persists for its rank when it is created/updated: the numeric {@code rankScore}
 * and the {@code international} flag. The rank <em>letter</em> is not stored — it is derived from these on read
 * (see {@link ObdxRank}).
 *
 * <p>The tier comes from the number of competitors; the international flag is set when at least one competitor's
 * (dog's) country differs from the event's country. The configuration's band places the score within the
 * 0–1000 scale (see {@link ObdxConfigurationsRankThresholds#eventScore(int, boolean)}); when the configuration
 * declares no band the score is {@code null}.
 */
public final class ObdxEventRank {

    private ObdxEventRank() {
    }

    /**
     * Whether a competitor counts as foreign relative to the event: it has a country and that country differs
     * (case-insensitively) from the event's. Competitors with no country never count as foreign.
     */
    public static boolean isForeign(String competitorCountry, String eventCountry) {
        return competitorCountry != null && !competitorCountry.isBlank()
                && !competitorCountry.equalsIgnoreCase(eventCountry);
    }

    /** Whether at least one of the competitors' countries is foreign relative to the event's country. */
    public static boolean isInternational(Collection<String> competitorCountries, String eventCountry) {
        return competitorCountries.stream().anyMatch(country -> isForeign(country, eventCountry));
    }

    /**
     * The event's automatic rank score for its configuration band, or {@code null} when the configuration
     * declares no band.
     */
    public static Integer eventScore(String configurationId, int competitorCount, boolean international) {
        ObdxConfigurationsRankThresholds band = ObdxConfigurationsRankThresholds.fromConfigurationId(configurationId);
        return band == null ? null : band.eventScore(competitorCount, international);
    }
}
