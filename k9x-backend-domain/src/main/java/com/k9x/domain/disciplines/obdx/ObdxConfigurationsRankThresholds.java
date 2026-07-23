package com.k9x.domain.disciplines.obdx;

import java.util.regex.Pattern;

/**
 * The ranking-score band {@code [min, max]} each OBDX configuration occupies within the global 0–1000 scale,
 * plus the rule that places an event's automatic {@code rank_score} inside that band from the competitor count
 * and the international flag (see {@link #eventScore(int, boolean)}).
 *
 * <p>The band is a fixed ranking rule that does not change between configuration <em>versions</em>, so it lives
 * here in code (keyed by the version-independent configuration id) rather than being duplicated across every
 * {@code configuration.json} version. Every {@code max} stays at or below {@link ObdxRank#MAX_AUTOMATIC_SCORE},
 * leaving the top slice ({@code S}, 901–1000) for the manually seeded exceptional rank.
 */
public enum ObdxConfigurationsRankThresholds {
    ENCI_PRE_DEBUTANTI("OBDX.ENCI_PREDEBUTTANTI", 50, 100),
    ENCI_DEBUTANTI("OBDX.ENCI_DEBUTTANTI", 100, 200),
    RSCE_DEBUTANTE("OBDX.RSCE_DEBUTANTE", 100, 200),
    CPC_COBS("OBDX.CPC_COBS", 100, 200),
    FCI_GRADE_1("OBDX.FCI_GRADE_1", 201, 400),
    RSCE_GRADE_1("OBDX.RSCE_GRADE_1", 201, 400),
    FCI_GRADE_2("OBDX.FCI_GRADE_2", 401, 600),
    FCI_GRADE_3("OBDX.FCI_GRADE_3", 601, 900);

    /**
     * Strips the trailing {@code .V<n>} version suffix, e.g. {@code OBDX.FCI_GRADE_3.V0 -> OBDX.FCI_GRADE_3}.
     */
    private static final Pattern VERSION_SUFFIX = Pattern.compile("\\.V\\d+$");

    private static final int TIER_COUNT = 5;
    private static final double TIERS_SHARE = 0.90;
    private static final double INTERNATIONAL_SHARE = 0.10;

    private final String configurationKey;
    private final int min;
    private final int max;

    ObdxConfigurationsRankThresholds(String configurationKey, int min, int max) {
        this.configurationKey = configurationKey;
        this.min = min;
        this.max = max;
    }

    /**
     * Competitor-count tier (1–5), a global rule independent of the configuration: {@code <5 → 1, [5,10) → 2,
     * [10,20) → 3, [20,35) → 4, ≥35 → 5}. Higher counts push the event higher inside its band.
     */
    public static int tierFromCompetitorCount(int competitorCount) {
        if (competitorCount >= 35) {
            return 5;
        }
        if (competitorCount >= 20) {
            return 4;
        }
        if (competitorCount >= 10) {
            return 3;
        }
        if (competitorCount >= 5) {
            return 2;
        }
        return 1;
    }

    /**
     * Resolves the band for a configuration id (with or without its {@code .V<n>} version suffix), or
     * {@code null} when the configuration has no band defined.
     */
    public static ObdxConfigurationsRankThresholds fromConfigurationId(String configurationId) {
        if (configurationId == null) {
            return null;
        }
        String key = VERSION_SUFFIX.matcher(configurationId).replaceFirst("");
        for (ObdxConfigurationsRankThresholds band : values()) {
            if (band.configurationKey.equals(key)) {
                return band;
            }
        }
        return null;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    /**
     * The event's automatic {@code rank_score} within this configuration's band: the competitor-count tier
     * (1–5) contributes {@code tier/5} of the band's lower 90%, and the international flag adds a flat 10% of
     * the band width on top, so {@code score = min + round(tier/5 · 0.9·range) + (international ? round(0.1·range) : 0)}.
     */
    public int eventScore(int competitorCount, boolean international) {
        int range = max - min;
        int tier = tierFromCompetitorCount(competitorCount);
        int tierPoints = (int) Math.round(((double) tier / TIER_COUNT) * TIERS_SHARE * range);
        int internationalPoints = international ? (int) Math.round(INTERNATIONAL_SHARE * range) : 0;
        return min + tierPoints + internationalPoints;
    }
}
