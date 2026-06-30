package com.k9x.application.stages.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.stages.use_case.command.UpdateStageCommand;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.competitions.commands.StageUpdateData;
import com.k9x.domain.stages.exceptions.StageNotFoundException;
import com.k9x.application.utils.auth.AuthAssertions;

public class UpdateStageServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final SaveCompetitionPersistencePort saveCompetitionPersistencePort;

    public UpdateStageServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                  SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.saveCompetitionPersistencePort = saveCompetitionPersistencePort;
    }

    public void updateStage(String stageId, UpdateStageCommand command, String userId, boolean organizer) {
        AuthAssertions.assertOrganizer(organizer, userId);
        String competitionId = getCompetitionPersistencePort.competitionIdByStage(stageId);
        if (competitionId == null) {
            throw new StageNotFoundException();
        }
        CompetitionAggregate competition =
                CompetitionAggregate.of(getCompetitionPersistencePort.getCompetition(competitionId));
        competition.renameStage(stageId, new StageUpdateData(command.name(), command.dateFrom(), command.dateTo()),
                userId, DateUtils.nowUtcMillis());
        saveCompetitionPersistencePort.save(competition);
    }
}
