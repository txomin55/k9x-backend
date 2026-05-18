package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.dto.FetchCompetitionDTO;
import com.k9x.application.competitions.port.GetCompetitionListPersistencePort;
import com.k9x.domain.aggregates.competitions.CompetitionStatus;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

import java.util.List;

public class GetCompetitionListServiceCase {

    private final GetCompetitionListPersistencePort getCompetitionListPersistencePort;

    public GetCompetitionListServiceCase(GetCompetitionListPersistencePort getCompetitionListPersistencePort) {
        this.getCompetitionListPersistencePort = getCompetitionListPersistencePort;
    }

    public List<FetchCompetitionDTO> getCompetitions(String userId, boolean organizer) {
        if (!organizer) {
            throw new UnauthorizedResourceException();
        }

        return getCompetitionListPersistencePort.getCompetitions(userId).stream()
                .map(c -> new FetchCompetitionDTO(c.id(), c.name(), c.description(), c.country(), c.address(),
                        CompetitionStatus.ACTIVE.name(), c.stages()))
                .toList();
    }
}
