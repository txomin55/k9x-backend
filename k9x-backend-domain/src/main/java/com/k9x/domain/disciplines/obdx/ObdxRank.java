package com.k9x.domain.disciplines.obdx;

/**
 * OBDX event rank derived from the number of competitors, with a {@code +} suffix when the event is
 * international (at least one competitor from a country other than the event's own). Ranks go from E
 * (fewest competitors) to A (most), yielding values E, E+, D, D+, C, C+, B, B+, A, A+.
 *
 * <p>This classification is OBDX-specific: other disciplines do not carry a rank.
 *
 * <h2>Ranking score</h2>
 * The primary, stored value is now the numeric {@code rank_score} (0–1000). Each configuration occupies a
 * band {@code [min, max]} of that scale, and within its band the event is placed by two factors:
 * <ul>
 *   <li>the {@link #tier()} from the competitor count contributes {@code tier/5 · 90%} of the band width, and</li>
 *   <li>the international flag adds a fixed {@code 10%} of the band width.</li>
 * </ul>
 * so {@code score = min + round(tier/5 · 0.9·range) + (international ? round(0.1·range) : 0)}.
 *
 * <p>The {@code rank} letter is then read off the <em>global</em> 0–1000 score bands
 * ({@link #fromScore(int)}: {@code E ≤ 200, D 201–400, C 401–600, B 601–800, A ≥ 801}), and the {@code +}
 * suffix marks an international event ({@link #labelFromScore(int, boolean)}). Because each configuration's
 * band lands inside one of these ranges, the letter reflects the event's category while the score positions it
 * within.
 *
 * <p>Automatic scores are confined to {@code [0, }{@link #MAX_AUTOMATIC_SCORE}{@code ]}. The top slice
 * {@code (950, 1000]} is reserved for the manually seeded {@link #EXCEPTIONAL} ({@code "A++"}) rank — so every
 * configuration band must keep its {@code max} at or below {@link #MAX_AUTOMATIC_SCORE}. A stored score above
 * that ceiling is only ever an A++ seed, and {@link #labelFromScore(int, boolean)} reports it as such.
 *
 * <p>{@link #EXCEPTIONAL} ({@code "A++"}) is never produced by {@link #fromCompetitorCount(int)}/
 * {@link #format(boolean)}/{@link #score(int, int, boolean)}; it is a manual, seed-only distinction (e.g. a
 * world-championship final).
 */
public enum ObdxRank {
    E, D, C, B, A;

    /**
     * Reserved, unreachable rank value that only ever originates from seed data.
     */
    public static final String EXCEPTIONAL = "A++";

    /**
     * Highest score the automatic formula may ever yield. The slice above this ceiling (up to 1000) is
     * reserved for the manually seeded {@link #EXCEPTIONAL} rank, so configuration bands must not exceed it.
     */
    public static final int MAX_AUTOMATIC_SCORE = 950;

    private static final String INTERNATIONAL_SUFFIX = "+";
    private static final int TIER_COUNT = 5;
    private static final double INTERNATIONAL_SHARE = 0.10;
    private static final double TIERS_SHARE = 0.90;

    // Global rank-score letter bands over the 0–1000 scale: E ≤ 200, D 201–400, C 401–600, B 601–800, A ≥ 801.
    private static final int A_MIN_SCORE = 801;
    private static final int B_MIN_SCORE = 601;
    private static final int C_MIN_SCORE = 401;
    private static final int D_MIN_SCORE = 201;

    /**
     * Resolves the rank letter from the competitor count:
     * E &lt; 5, D [5,10), C [10,20), B [20,35), A &ge; 35.
     */
    public static ObdxRank fromCompetitorCount(int competitorCount) {
        if (competitorCount >= 35) {
            return A;
        }
        if (competitorCount >= 20) {
            return B;
        }
        if (competitorCount >= 10) {
            return C;
        }
        if (competitorCount >= 5) {
            return D;
        }
        return E;
    }

    /**
     * The rank letter, appending {@code +} when the event is international.
     */
    public String format(boolean international) {
        return international ? name() + INTERNATIONAL_SUFFIX : name();
    }

    /**
     * This rank's tier weight: {@code E=1, D=2, C=3, B=4, A=5}.
     */
    public int tier() {
        return ordinal() + 1;
    }

    /**
     * The numeric ranking score for this rank within the configuration's band {@code [min, max]}. The tier
     * contributes {@code tier/5} of the band's lower 90%, and the international flag adds a flat 10% of the
     * band width on top.
     */
    public int score(int min, int max, boolean international) {
        int range = max - min;
        int tierPoints = (int) Math.round(((double) tier() / TIER_COUNT) * TIERS_SHARE * range);
        int internationalPoints = international ? (int) Math.round(INTERNATIONAL_SHARE * range) : 0;
        return min + tierPoints + internationalPoints;
    }

    /**
     * The rank letter for a score, read off the global 0–1000 bands (independent of any configuration):
     * {@code E ≤ 200, D 201–400, C 401–600, B 601–800, A ≥ 801}. Since each configuration's band sits inside
     * one of these ranges, the letter effectively reflects the event's category, while the competitor count
     * moves the score within it.
     */
    public static ObdxRank fromScore(int rankScore) {
        if (rankScore >= A_MIN_SCORE) {
            return A;
        }
        if (rankScore >= B_MIN_SCORE) {
            return B;
        }
        if (rankScore >= C_MIN_SCORE) {
            return C;
        }
        if (rankScore >= D_MIN_SCORE) {
            return D;
        }
        return E;
    }

    /**
     * Derives the full rank label (e.g. {@code "B+"}, {@code "D"}) from a stored {@code rankScore}: the
     * letter comes from the global bands ({@link #fromScore(int)}) and the {@code +} suffix from the
     * international flag. A score above {@link #MAX_AUTOMATIC_SCORE} can only be a manual seed and is
     * reported as the exceptional {@link #EXCEPTIONAL} ({@code "A++"}).
     */
    public static String labelFromScore(int rankScore, boolean international) {
        if (rankScore > MAX_AUTOMATIC_SCORE) {
            return EXCEPTIONAL;
        }
        return fromScore(rankScore).format(international);
    }
}
