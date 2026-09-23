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
    E(0, 200),
    D(201, 400),
    C(401, 600),
    B(601, 800),
    A(801, 900),
    S(901, 1000);

    /** Floor of the global rank scale. */
    public static final int SCALE_MIN = 0;
    /** Ceiling of the global rank scale. */
    public static final int SCALE_MAX = 1000;

    private final int minScore;
    private final int maxScore;

    ObdxRank(int minScore, int maxScore) {
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    /** Inclusive lower bound of the letter on the global scale. */
    public int minScore() {
        return minScore;
    }

    /** Inclusive upper bound of the letter on the global scale. */
    public int maxScore() {
        return maxScore;
    }

    /**
     * The rank letter for a score, read off the global 0–1000 bands (independent of any configuration):
     * {@code E ≤ 200, D 201–400, C 401–600, B 601–800, A 801–900, S ≥ 901}.
     */
    public static ObdxRank fromScore(int rankScore) {
        ObdxRank[] letters = values();
        for (int i = letters.length - 1; i > 0; i--) {
            if (rankScore >= letters[i].minScore) {
                return letters[i];
            }
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
