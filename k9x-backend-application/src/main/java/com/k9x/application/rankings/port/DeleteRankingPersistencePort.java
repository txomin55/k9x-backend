package com.k9x.application.rankings.port;

public interface DeleteRankingPersistencePort {

    /**
     * Physically deletes the row. Rankings have no soft delete.
     */
    void deleteRanking(String id);
}
