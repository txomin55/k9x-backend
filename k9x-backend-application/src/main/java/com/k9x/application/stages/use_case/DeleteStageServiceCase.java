package com.k9x.application.stages.use_case;

import com.k9x.application.competitions.CompetitionNavigator;
import com.k9x.application.competitions.exceptions.CompetitionAlreadyDeletedException;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.application.stages.exceptions.StageCannotBeDeletedException;
import com.k9x.application.stages.exceptions.StageNotFoundException;
import com.k9x.application.stages.port.DeleteStagePersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.domain.aggregates.stages.StageStatus;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class DeleteStageServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final DeleteStagePersistencePort deleteStagePersistencePort;

    public DeleteStageServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                  DeleteStagePersistencePort deleteStagePersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.deleteStagePersistencePort = deleteStagePersistencePort;
    }

    public void deleteStage(String stageId, String userId, boolean organizer) {
        assertOrganizer(organizer);
        String competitionId = getCompetitionPersistencePort.competitionIdByStage(stageId);
        if (competitionId == null) {
            throw new StageNotFoundException();
        }
        Competition competition = getCompetitionPersistencePort.getCompetition(competitionId);
        Stage stage = CompetitionNavigator.findStage(competition, stageId);
        assertStageValidations(stage, userId);
        assertCompetitionValidations(competition, userId);
        assertStageIsDeletable(stage);
        deleteStagePersistencePort.deleteStage(stageId, DateUtils.nowUtcMillis());
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

    private void assertCompetitionValidations(Competition competition, String userId) {
        if (competition.deletedAt() != null) {
            throw new CompetitionAlreadyDeletedException();
        }
        if (!competition.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
    }

    private void assertStageIsDeletable(Stage stage) {
        StageStatus status = stage.status(DateUtils.nowUtcMillis());
        if (status == StageStatus.STARTED || status == StageStatus.FINISHED) {
            throw new StageCannotBeDeletedException();
        }
    }
}
