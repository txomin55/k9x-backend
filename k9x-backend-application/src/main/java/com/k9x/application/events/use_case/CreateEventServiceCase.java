package com.k9x.application.events.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.competitions.commands.NewEventData;
import com.k9x.domain.stages.exceptions.StageNotFoundException;
import com.k9x.application.utils.auth.AuthAssertions;
import com.k9x.application.shared.TransactionalUseCase;

public class CreateEventServiceCase implements TransactionalUseCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final SaveCompetitionPersistencePort saveCompetitionPersistencePort;

    public CreateEventServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                  SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.saveCompetitionPersistencePort = saveCompetitionPersistencePort;
    }

    public void createEvent(String id, String name, String stageId, String disciplineId, String userId, boolean organizer) {
        AuthAssertions.assertOrganizer(organizer, userId);
        String competitionId = getCompetitionPersistencePort.competitionIdByStage(stageId);
        if (competitionId == null) {
            throw new StageNotFoundException();
        }
        CompetitionAggregate competition =
                CompetitionAggregate.of(getCompetitionPersistencePort.getCompetition(competitionId));
        competition.createEvent(new NewEventData(id, name, stageId, disciplineId), userId, DateUtils.nowUtcMillis());
        saveCompetitionPersistencePort.save(competition);
    }
}
