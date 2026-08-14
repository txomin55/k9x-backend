package com.k9x.domain.disciplines.obdx;

/**
 * OBDX event rank <em>letter</em>, read off the numeric {@code rank_score} on the global 0–1000 scale.
 *
 * <p>This classification is OBDX-specific: other disciplines do not carry a rank. The letter is a label
 * <em>derived</em> from the stored {@code rank_score}; it is never persisted. The numeric score itself is
 * produced by {@link ObdxConfigurationsRankThresholds#eventScore(int, ObdxEventCategory)} (the event's category
 * sub-band positioned by the competitor count) — that is a different concern and does not live here.
 *
 * <h2>Global letter bands</h2>
 * {@link #fromScore(int)} maps a score to its letter: {@code E ≤ 200, D 201–400, C 401–600, B 601–800,
 * A 801–900, S 901–1000}. Because each configuration's band lands inside one of these ranges, the letter
 * reflects the grade of the trial while the score positions it within.
 *
 * <p>{@link #S} ({@code 901–1000}) is reached only by a world championship final
 * ({@link ObdxEventCategory#WC_FINAL}, a fixed 1000), so it is produced by the formula like every other letter
 * rather than being seeded by hand.
 */
public enum ObdxRank {
    E, D, C, B, A, S;

    // Global rank-score letter bands over the 0–1000 scale: E ≤ 200, D 201–400, C 401–600, B 601–800,
    // A 801–900, S 901–1000.
    private static final int S_MIN_SCORE = 901;
    private static final int A_MIN_SCORE = 801;
    private static final int B_MIN_SCORE = 601;
    private static final int C_MIN_SCORE = 401;
    private static final int D_MIN_SCORE = 201;

    /**
     * The rank letter for a score, read off the global 0–1000 bands (independent of any configuration):
     * {@code E ≤ 200, D 201–400, C 401–600, B 601–800, A 801–900, S ≥ 901}.
     */
    public static ObdxRank fromScore(int rankScore) {
        if (rankScore >= S_MIN_SCORE) {
            return S;
        }
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
     * Derives the rank label (e.g. {@code "B"}, {@code "S"}) from a stored {@code rankScore} by reading the
     * global letter bands.
     */
    public static String labelFromScore(int rankScore) {
        return fromScore(rankScore).name();
    }
}
