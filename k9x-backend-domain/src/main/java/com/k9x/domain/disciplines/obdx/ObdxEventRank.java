package com.k9x.domain.disciplines.obdx;

import java.util.Collection;

/**
 * Computes what an OBDX event persists for its rank when it is created/updated: the numeric {@code rankScore}
 * and the {@code international} flag. The rank <em>letter</em> is not stored — it is derived from these on read
 * (see {@link ObdxRank}).
 *
 * <p>The tier comes from the number of competitors; the international flag is set when enough competitors'
 * (dogs') countries differ from the event's country — how many is enough depends on the tier (see
 * {@link #requiredForeignCompetitors(int)}). The configuration's band places the score within the
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

    /**
     * How many foreign competitors an event needs to count as international, by competitor-count tier
     * (see {@link ObdxConfigurationsRankThresholds#tierFromCompetitorCount(int)}): roughly 10% of the tier's
     * size, rounded up — {@code tier 1 → 1, tier 2 → 2, tier 3 → 2, tier 4 → 3, tier 5 → 4}. A bigger event
     * needs more foreign competitors before the flag is earned; a single visitor is no longer enough.
     */
    private static final int[] REQUIRED_FOREIGN_BY_TIER = {1, 2, 2, 3, 4};

    /** The number of foreign competitors required for an event of this size to count as international. */
    public static int requiredForeignCompetitors(int competitorCount) {
        return REQUIRED_FOREIGN_BY_TIER[ObdxConfigurationsRankThresholds.tierFromCompetitorCount(competitorCount) - 1];
    }

    /**
     * Whether the event is international: at least {@link #requiredForeignCompetitors(int)} of its competitors
     * have a country that is foreign relative to the event's country. The collection carries one entry per
     * competitor (a {@code null} entry for a competitor with no known country), so its size is the event's
     * competitor count.
     */
    public static boolean isInternational(Collection<String> competitorCountries, String eventCountry) {
        long foreign = competitorCountries.stream().filter(country -> isForeign(country, eventCountry)).count();
        return foreign >= requiredForeignCompetitors(competitorCountries.size());
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
