package com.k9x.application.rankings.use_case;

import com.k9x.application.rankings.port.GetRankingListPersistencePort;
import com.k9x.application.rankings.use_case.dto.FetchRankingListItemDTO;
import com.k9x.application.utils.auth.AuthAssertions;

import java.util.List;

/**
 * Read-only list of the organizer's own rankings. The creator is always the authenticated user: unlike judges
 * there is no "everyone's rankings" mode, since a ranking is a private configuration.
 */
public class GetRankingListServiceCase {

    private final GetRankingListPersistencePort getRankingListPersistencePort;

    public GetRankingListServiceCase(GetRankingListPersistencePort getRankingListPersistencePort) {
        this.getRankingListPersistencePort = getRankingListPersistencePort;
    }

    public List<FetchRankingListItemDTO> getRankings(String userId, boolean organizer) {
        AuthAssertions.assertOrganizer(organizer, userId);
        return getRankingListPersistencePort.getRankings(userId);
    }
}
