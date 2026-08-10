package com.k9x.application.rankings.port;

import com.k9x.domain.rankings.aggregates.Ranking;

public interface GetRankingPersistencePort {

    /**
     * Returns the plain aggregate used by the write guards, or {@code null} when it does not exist.
     */
    Ranking getRanking(String id);
}
