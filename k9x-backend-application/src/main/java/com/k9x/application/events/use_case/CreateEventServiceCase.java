package com.k9x.application.events.use_case;

import com.k9x.application.competitions.CompetitionNavigator;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.events.obdx.use_case.port.CreateObdxEventPersistencePort;
import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.application.stages.exceptions.StageNotFoundException;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class CreateEventServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final CreateObdxEventPersistencePort createObdxEventPersistencePort;

    public CreateEventServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                  CreateObdxEventPersistencePort createObdxEventPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.createObdxEventPersistencePort = createObdxEventPersistencePort;
    }

    public void createEvent(String id, String name, String stageId, String disciplineId, String userId, boolean organizer) {
        assertOrganizer(organizer);
        String competitionId = getCompetitionPersistencePort.competitionIdByStage(stageId);
        if (competitionId == null) {
            throw new StageNotFoundException();
        }
        Competition competition = getCompetitionPersistencePort.getCompetition(competitionId);
        Stage stage = CompetitionNavigator.findStage(competition, stageId);
        assertStageValidations(stage, userId);
        createObdxEventPersistencePort.createEvent(id, name, stageId, disciplineId, userId, DateUtils.nowUtcMillis());
    }

    private void assertOrganizer(boolean organizer) {
        if (!organizer) {
            throw new UnauthorizedResourceException();
        }
    }

    private void assertStageValidations(Stage stage, String userId) {
        if (stage == null) {
            throw new StageNotFoundException();
        }
        if (stage.deletedAt() != null) {
            throw new StageAlreadyDeletedException();
        }
        if (!stage.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
    }
}
