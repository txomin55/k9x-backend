package com.k9x.domain.disciplines.obdx;

/**
 * OBDX event rank derived from the number of competitors, with a {@code +} suffix when the event is
 * international (at least one competitor from a country other than the event's own). Ranks go from E
 * (fewest competitors) to A (most), yielding stored values E, E+, D, D+, C, C+, B, B+, A, A+.
 *
 * <p>This classification is OBDX-specific: other disciplines do not carry a rank.
 *
 * <p>The stored {@code rank} column may also hold the reserved value {@link #EXCEPTIONAL} ({@code "A++"}).
 * It is never produced by {@link #fromCompetitorCount(int)}/{@link #format(boolean)}; it is a manual,
 * seed-only distinction (e.g. a world-championship final) kept here purely for consistency with the values
 * the database can contain.
 */
public enum ObdxRank {
    E, D, C, B, A;

    /**
     * Reserved, unreachable rank value that only ever originates from seed data.
     */
    public static final String EXCEPTIONAL = "A++";
    private static final String INTERNATIONAL_SUFFIX = "+";

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
     * The stored rank value, appending {@code +} when the event is international.
     */
    public String format(boolean international) {
        return international ? name() + INTERNATIONAL_SUFFIX : name();
    }
}
