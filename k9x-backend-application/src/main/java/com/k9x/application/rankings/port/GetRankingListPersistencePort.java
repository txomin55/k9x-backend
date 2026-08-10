package com.k9x.application.rankings.port;

import com.k9x.application.rankings.use_case.dto.FetchRankingListItemDTO;

import java.util.List;

public interface GetRankingListPersistencePort {

    /**
     * Rankings owned by the given creator, newest first.
     */
    List<FetchRankingListItemDTO> getRankings(String creator);
}
