package com.k9x.application.events.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.events.obdx.use_case.BihGuards;
import com.k9x.application.events.obdx.use_case.command.EnrollObdxEventCommand;
import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.domain.events.exceptions.EventNotFoundException;

public class EnrollEventServiceCase implements TransactionalUseCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final SaveCompetitionPersistencePort saveCompetitionPersistencePort;
    private final GetDogPersistencePort getDogPersistencePort;

    public EnrollEventServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                  SaveCompetitionPersistencePort saveCompetitionPersistencePort,
                                  GetDogPersistencePort getDogPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.saveCompetitionPersistencePort = saveCompetitionPersistencePort;
        this.getDogPersistencePort = getDogPersistencePort;
    }

    public void enrollEvent(String eventId, EnrollObdxEventCommand command, String userId) {
        String competitionId = getCompetitionPersistencePort.competitionIdByEvent(eventId);
        if (competitionId == null) {
            throw new EventNotFoundException();
        }
        Dog dog = getDogPersistencePort.getDog(command.dogId());
        BihGuards.assertBihAllowedForSex(command.bih(), dog);
        CompetitionAggregate competition =
                CompetitionAggregate.of(getCompetitionPersistencePort.getCompetition(competitionId));
        competition.enrollDog(eventId, command.dogId(), command.bih(), userId, DateUtils.nowUtcMillis());
        saveCompetitionPersistencePort.save(competition);
    }
}
