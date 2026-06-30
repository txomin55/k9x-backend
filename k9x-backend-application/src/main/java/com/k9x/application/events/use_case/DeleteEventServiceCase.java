package com.k9x.application.events.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.domain.shared.SupportUser;

public class DeleteEventServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final SaveCompetitionPersistencePort saveCompetitionPersistencePort;

    public DeleteEventServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                  SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.saveCompetitionPersistencePort = saveCompetitionPersistencePort;
    }

    public void deleteEvent(String id, String userId, boolean organizer) {
        assertOrganizer(organizer, userId);
        String competitionId = getCompetitionPersistencePort.competitionIdByEvent(id);
        if (competitionId == null) {
            throw new EventNotFoundException();
        }
        CompetitionAggregate competition =
                CompetitionAggregate.of(getCompetitionPersistencePort.getCompetition(competitionId));
        competition.deleteEvent(id, userId, DateUtils.nowUtcMillis());
        saveCompetitionPersistencePort.save(competition);
    }

    private void assertOrganizer(boolean organizer, String userId) {
        if (!organizer && !SupportUser.is(userId)) {
            throw new UnauthorizedResourceException();
        }
    }
}
