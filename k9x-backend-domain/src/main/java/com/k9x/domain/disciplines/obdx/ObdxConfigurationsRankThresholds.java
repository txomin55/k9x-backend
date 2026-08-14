package com.k9x.domain.disciplines.obdx;

import java.util.regex.Pattern;

/**
 * The ranking-score band {@code [min, max]} each OBDX configuration occupies within the global 0–1000 scale,
 * plus the rule that places an event's automatic {@code rank_score} inside that band from its
 * {@link ObdxEventCategory} and its competitor count (see {@link #eventScore(int, ObdxEventCategory)}).
 *
 * <p>The band is a fixed ranking rule that does not change between configuration <em>versions</em>, so it lives
 * here in code (keyed by the version-independent configuration id) rather than being duplicated across every
 * {@code configuration.json} version.
 *
 * <h2>Category sub-bands</h2>
 * The category subdivides the configuration's band, so the score a trial can reach depends on its competitive
 * tier. Grades that hold no world championship split their band between {@code CLUB} (the lower
 * {@link #CLUB_SHARE 75%}) and {@code OPEN} (the top quarter). FCI grade 3 additionally hosts the three world
 * championship rounds, which are <em>fixed points</em>: a final is worth {@link #WC_FINAL_SCORE 1000} however
 * many competitors turn up.
 */
public enum ObdxConfigurationsRankThresholds {
    ENCI_PRE_DEBUTTANTI("OBDX.ENCI_PREDEBUTTANTI", 50, 100, false),
    ENCI_DEBUTTANTI("OBDX.ENCI_DEBUTTANTI", 100, 200, false),
    RSCE_DEBUTANTE("OBDX.RSCE_DEBUTANTE", 100, 200, false),
    CPC_COBS("OBDX.CPC_COBS", 100, 200, false),
    FCI_GRADE_1("OBDX.FCI_GRADE_1", 201, 400, false),
    RSCE_GRADO_1("OBDX.RSCE_GRADO_1", 201, 400, false),
    FCI_GRADE_2("OBDX.FCI_GRADE_2", 401, 600, false),
    FCI_GRADE_3("OBDX.FCI_GRADE_3", 601, 1000, true);

    /** A closed score range. World championship rounds use a degenerate band where {@code min == max}. */
    public record Band(int min, int max) {
    }

    /**
     * Strips the trailing {@code .V<n>} version suffix, e.g. {@code OBDX.FCI_GRADE_3.V0 -> OBDX.FCI_GRADE_3}.
     */
    private static final Pattern VERSION_SUFFIX = Pattern.compile("\\.V\\d+$");

    private static final int TIER_COUNT = 3;

    /** Share of a non-championship band that belongs to {@code CLUB}; {@code OPEN} takes the rest. */
    private static final double CLUB_SHARE = 0.75;

    // The FCI grade 3 layout: CLUB [601, 700], OPEN [701, 750], and the three championship rounds as fixed
    // points. The gaps between them are intentional — nothing but a championship round scores there.
    private static final int GRADE_3_CLUB_MAX = 700;
    private static final int GRADE_3_OPEN_MAX = 750;
    private static final int WC_QUALIFIER_SCORE = 800;
    private static final int WC_SEMI_FINAL_SCORE = 900;
    private static final int WC_FINAL_SCORE = 1000;

    private final String configurationKey;
    private final int min;
    private final int max;
    private final boolean worldChampionship;

    ObdxConfigurationsRankThresholds(String configurationKey, int min, int max, boolean worldChampionship) {
        this.configurationKey = configurationKey;
        this.min = min;
        this.max = max;
        this.worldChampionship = worldChampionship;
    }

    /**
     * Competitor-count tier (1–3), a global rule independent of the configuration: {@code <10 → 1,
     * [10,25) → 2, ≥25 → 3}. Higher counts push the event higher inside its sub-band.
     */
    public static int tierFromCompetitorCount(int competitorCount) {
        if (competitorCount >= 25) {
            return 3;
        }
        if (competitorCount >= 10) {
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
     * Whether this configuration may be run under the given category. Only the grade that hosts the world
     * championship accepts the {@code WC_*} rounds; every other grade is limited to {@code CLUB} and
     * {@code OPEN}. This is the single source of the rule — both the write-side validation and the category
     * catalogue read it.
     */
    public boolean allows(ObdxEventCategory category) {
        return category != null
                && (worldChampionship || category == ObdxEventCategory.CLUB || category == ObdxEventCategory.OPEN);
    }

    /**
     * The slice of this configuration's band that the given category may reach.
     *
     * @throws IllegalArgumentException when the category is not {@link #allows(ObdxEventCategory) allowed} —
     *                                  callers validate the combination before scoring.
     */
    public Band subBand(ObdxEventCategory category) {
        if (!allows(category)) {
            throw new IllegalArgumentException(
                    "Category " + category + " is not allowed for configuration " + configurationKey);
        }
        if (worldChampionship) {
            return switch (category) {
                case CLUB -> new Band(min, GRADE_3_CLUB_MAX);
                case OPEN -> new Band(GRADE_3_CLUB_MAX + 1, GRADE_3_OPEN_MAX);
                case WC_Q -> new Band(WC_QUALIFIER_SCORE, WC_QUALIFIER_SCORE);
                case WC_SEMI -> new Band(WC_SEMI_FINAL_SCORE, WC_SEMI_FINAL_SCORE);
                case WC_FINAL -> new Band(WC_FINAL_SCORE, WC_FINAL_SCORE);
            };
        }
        int clubMax = min + (int) Math.round(CLUB_SHARE * (max - min));
        return category == ObdxEventCategory.CLUB ? new Band(min, clubMax) : new Band(clubMax + 1, max);
    }

    /**
     * The event's automatic {@code rank_score}: the competitor-count tier (1–3) contributes {@code tier/3} of
     * the category's sub-band width on top of its floor, so
     * {@code score = subMin + round(tier/3 · (subMax - subMin))}.
     *
     * <p>The tier deliberately never lands on the sub-band floor: that floor is also the floor a competitor's
     * own score is measured against ({@link ObdxCompetitorEventScore}), so an event sitting exactly on it would
     * leave every qualified competitor tied. World championship rounds have a zero-width sub-band, so the tier
     * does not move them at all.
     */
    public int eventScore(int competitorCount, ObdxEventCategory category) {
        Band band = subBand(category);
        int range = band.max() - band.min();
        int tier = tierFromCompetitorCount(competitorCount);
        return band.min() + (int) Math.round(((double) tier / TIER_COUNT) * range);
    }
}
