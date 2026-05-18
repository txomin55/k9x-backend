package com.k9x.application.stages.use_case;

import com.k9x.application.competitions.exceptions.CompetitionAlreadyDeletedException;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.stages.command.UpdateStageCommand;
import com.k9x.application.stages.payload.UpdateStagePersistencePayload;
import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.application.stages.exceptions.StageNotFoundException;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.application.stages.port.UpdateStagePersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class UpdateStageServiceCase {

    private final GetStagePersistencePort getStagePersistencePort;
    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final UpdateStagePersistencePort updateStagePersistencePort;

    public UpdateStageServiceCase(GetStagePersistencePort getStagePersistencePort,
                                  GetCompetitionPersistencePort getCompetitionPersistencePort,
                                  UpdateStagePersistencePort updateStagePersistencePort) {
        this.getStagePersistencePort = getStagePersistencePort;
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.updateStagePersistencePort = updateStagePersistencePort;
    }

    public void updateStage(String stageId, UpdateStageCommand command, String userId, boolean organizer) {
        assertOrganizer(organizer);
        Stage stage = getStagePersistencePort.getStage(stageId);
        assertStageValidations(stage, userId);
        Competition competition = getCompetitionPersistencePort.getCompetition(stage.competitionId());
        assertCompetitionValidations(competition, userId);
        updateStagePersistencePort.updateStage(stageId, new UpdateStagePersistencePayload(command.name(), command.dateFrom(), command.dateTo(), DateUtils.nowUtcMillis()));
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
}
