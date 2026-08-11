package com.k9x.application.rankings.port;

import com.k9x.application.shared.IdNameDTO;

import java.util.List;

public interface GetEventRankingsPersistencePort {

    /** Rankings that include the given event, ids and names only. */
    List<IdNameDTO> getEventRankings(String eventId);
}
