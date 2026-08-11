package com.k9x.application.rankings.use_case;

import com.k9x.application.rankings.port.GetEventRankingsPersistencePort;
import com.k9x.application.shared.IdNameDTO;
import com.k9x.domain.rankings.RankingIds;

import java.util.List;

/**
 * Public read of the rankings an event takes part in.
 *
 * <p>A ranking derived from a competition is discoverable from the competition itself, so its identifier is
 * public. A standalone one is not: its identifier is only handed out to an authenticated caller, and comes
 * back null otherwise, so the name can still be shown without exposing a link to it.
 */
public class GetEventRankingsServiceCase {

    private final GetEventRankingsPersistencePort getEventRankingsPersistencePort;

    public GetEventRankingsServiceCase(GetEventRankingsPersistencePort getEventRankingsPersistencePort) {
        this.getEventRankingsPersistencePort = getEventRankingsPersistencePort;
    }

    public List<IdNameDTO> getEventRankings(String eventId, boolean authenticated) {
        return getEventRankingsPersistencePort.getEventRankings(eventId).stream()
                .map(ranking -> RankingIds.isCompetitionRanking(ranking.id()) || authenticated
                        ? ranking
                        : new IdNameDTO(null, ranking.name()))
                .toList();
    }
}
