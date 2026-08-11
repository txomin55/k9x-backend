package com.k9x.domain.rankings;

/**
 * A ranking's identifier says where it came from: one derived from a competition is prefixed, because its id
 * is built from the competition's own. That distinction drives two rules, so it lives here rather than being
 * spelled out at each call site.
 */
public final class RankingIds {

    public static final String COMPETITION_PREFIX = "ranking_competition";

    private RankingIds() {}

    /** Whether the ranking belongs to a competition, and so is managed from that competition's tab. */
    public static boolean isCompetitionRanking(String rankingId) {
        return rankingId != null && rankingId.startsWith(COMPETITION_PREFIX);
    }
}
