package com.k9x.domain.disciplines.obdx;

/**
 * OBDX event rank <em>letter</em>, read off the numeric {@code rank_score} on the global 0–1000 scale, with a
 * {@code +} suffix when the event is international.
 *
 * <p>This classification is OBDX-specific: other disciplines do not carry a rank. The letter is a label
 * <em>derived</em> from the stored {@code rank_score}; it is never persisted. The numeric score itself is
 * produced by {@link ObdxConfigurationsRankThresholds#eventScore(int, boolean)} (competitor count + international
 * placed inside the configuration's band) — that is a different concern and does not live here.
 *
 * <h2>Global letter bands</h2>
 * {@link #fromScore(int)} maps a score to its letter: {@code E ≤ 200, D 201–400, C 401–600, B 601–800,
 * A 801–900, S 901–1000}. Because each configuration's band lands inside one of these ranges, the letter
 * reflects the event's category while the score positions it within.
 *
 * <p>{@link #S} ({@code 901–1000}) is the exceptional, always-international rank (so it renders as {@code "S+"}).
 * It is never produced by the automatic formula: automatic scores are confined to
 * {@code [0, }{@link #MAX_AUTOMATIC_SCORE}{@code ]}, so every configuration band must keep its {@code max} at or
 * below {@link #MAX_AUTOMATIC_SCORE}. A rank_score in the {@code S} range is only ever assigned by hand (seed),
 * e.g. a world-championship final.
 */
public enum ObdxRank {
    E, D, C, B, A, S;

    /**
     * Highest score the automatic formula may ever yield. The slice above this ceiling (up to 1000) is the
     * {@link #S} range, assigned manually, so configuration bands must not exceed it.
     */
    public static final int MAX_AUTOMATIC_SCORE = 900;

    private static final String INTERNATIONAL_SUFFIX = "+";

    // Global rank-score letter bands over the 0–1000 scale: E ≤ 200, D 201–400, C 401–600, B 601–800,
    // A 801–900, S 901–1000.
    private static final int S_MIN_SCORE = 901;
    private static final int A_MIN_SCORE = 801;
    private static final int B_MIN_SCORE = 601;
    private static final int C_MIN_SCORE = 401;
    private static final int D_MIN_SCORE = 201;

    /**
     * The rank letter for a score, read off the global 0–1000 bands (independent of any configuration):
     * {@code E ≤ 200, D 201–400, C 401–600, B 601–800, A 801–900, S ≥ 901}. Scores in the {@link #S} range are
     * manual only.
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
     * The rank letter, appending {@code +} when the event is international. {@link #S} is always international,
     * so it always renders as {@code "S+"}.
     */
    public String format(boolean international) {
        return international ? name() + INTERNATIONAL_SUFFIX : name();
    }

    /**
     * Derives the full rank label (e.g. {@code "B+"}, {@code "D"}, {@code "S+"}) from a stored {@code rankScore}:
     * the letter comes from the global bands ({@link #fromScore(int)}) and the {@code +} suffix from the
     * international flag.
     */
    public static String labelFromScore(int rankScore, boolean international) {
        return fromScore(rankScore).format(international);
    }
}
