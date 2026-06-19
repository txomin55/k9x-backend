package com.k9x.application.events.obdx.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.events.obdx.use_case.command.UpdateNotCompetingCommand;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class UpdateNotCompetingServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final SaveCompetitionPersistencePort saveCompetitionPersistencePort;

    public UpdateNotCompetingServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                         SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.saveCompetitionPersistencePort = saveCompetitionPersistencePort;
    }

    public void updateNotCompeting(String eventId, UpdateNotCompetingCommand command, String userId, boolean organizer) {
        if (!organizer) {
            throw new UnauthorizedResourceException();
        }
        String competitionId = getCompetitionPersistencePort.competitionIdByEvent(eventId);
        if (competitionId == null) {
            throw new EventNotFoundException();
        }
        CompetitionAggregate competition =
                CompetitionAggregate.of(getCompetitionPersistencePort.getCompetition(competitionId));
        competition.updateCompetitorNotCompeting(eventId, command.dogId(), command.notCompeting(), userId,
                DateUtils.nowUtcMillis());
        saveCompetitionPersistencePort.save(competition);
    }
}
