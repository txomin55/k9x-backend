package com.k9x.application.stages.use_case;

import com.k9x.application.competitions.exceptions.CompetitionAlreadyDeletedException;
import com.k9x.application.competitions.exceptions.CompetitionNotFoundException;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.stages.port.CreateStagePersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class CreateStageServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final CreateStagePersistencePort createStagePersistencePort;

    public CreateStageServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                  CreateStagePersistencePort createStagePersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.createStagePersistencePort = createStagePersistencePort;
    }

    public void createStage(String id, String name, String competitionId, Long dateFrom, Long dateTo,
                            String userId, boolean organizer) {
        assertOrganizer(organizer);
        Competition competition = getCompetitionPersistencePort.getCompetition(competitionId);
        assertCompetitionValidations(competition, userId);
        createStagePersistencePort.createStage(id, name, competitionId, dateFrom, dateTo, userId, DateUtils.nowUtcMillis());
    }

    private void assertOrganizer(boolean organizer) {
        if (!organizer) {
            throw new UnauthorizedResourceException();
        }
    }

    private void assertCompetitionValidations(Competition competition, String userId) {
        if (competition == null) {
            throw new CompetitionNotFoundException();
        }
        if (competition.deletedAt() != null) {
            throw new CompetitionAlreadyDeletedException();
        }
        if (!competition.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
    }
}
