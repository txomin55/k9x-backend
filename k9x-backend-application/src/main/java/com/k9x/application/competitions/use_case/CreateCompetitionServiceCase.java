package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.domain.shared.SupportUser;

public class CreateCompetitionServiceCase {

    private final SaveCompetitionPersistencePort saveCompetitionPersistencePort;

    public CreateCompetitionServiceCase(SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        this.saveCompetitionPersistencePort = saveCompetitionPersistencePort;
    }

    public void createCompetition(String id, String name, String userId, boolean organizer) {
        assertOrganizer(organizer, userId);
        CompetitionAggregate competition = CompetitionAggregate.createNew(id, name, userId, DateUtils.nowUtcMillis());
        saveCompetitionPersistencePort.save(competition);
    }

    private void assertOrganizer(boolean organizer, String userId) {
        if (!organizer && !SupportUser.is(userId)) {
            throw new UnauthorizedResourceException();
        }
    }
}
