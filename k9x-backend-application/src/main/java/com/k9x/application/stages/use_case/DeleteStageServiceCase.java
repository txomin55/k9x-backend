package com.k9x.application.stages.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.stages.exceptions.StageNotFoundException;
import com.k9x.application.utils.auth.AuthAssertions;

public class DeleteStageServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final SaveCompetitionPersistencePort saveCompetitionPersistencePort;

    public DeleteStageServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                  SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.saveCompetitionPersistencePort = saveCompetitionPersistencePort;
    }

    public void deleteStage(String stageId, String userId, boolean organizer) {
        AuthAssertions.assertOrganizer(organizer, userId);
        String competitionId = getCompetitionPersistencePort.competitionIdByStage(stageId);
        if (competitionId == null) {
            throw new StageNotFoundException();
        }
        CompetitionAggregate competition =
                CompetitionAggregate.of(getCompetitionPersistencePort.getCompetition(competitionId));
        competition.deleteStage(stageId, userId, DateUtils.nowUtcMillis());
        saveCompetitionPersistencePort.save(competition);
    }
}
