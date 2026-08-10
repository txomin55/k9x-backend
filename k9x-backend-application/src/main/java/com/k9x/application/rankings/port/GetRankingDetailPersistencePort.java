package com.k9x.application.rankings.port;

import com.k9x.application.rankings.use_case.dto.FetchRankingDTO;

public interface GetRankingDetailPersistencePort {

    /**
     * Read model for the detail endpoint, with the event names already resolved. Scoped by creator, so
     * another organizer's ranking behaves as if it did not exist. Returns {@code null} when not found.
     */
    FetchRankingDTO getRankingDetail(String id, String creator);
}
