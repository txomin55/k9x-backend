package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.port.CreateCompetitionPersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class CreateCompetitionServiceCase {

    private final CreateCompetitionPersistencePort createCompetitionPersistencePort;

    public CreateCompetitionServiceCase(CreateCompetitionPersistencePort createCompetitionPersistencePort) {
        this.createCompetitionPersistencePort = createCompetitionPersistencePort;
    }

    public void createCompetition(String id, String name, String userId, boolean organizer) {
        assertOrganizer(organizer);
        createCompetitionPersistencePort.createCompetition(id, name, userId, DateUtils.nowUtcMillis());
    }

    private void assertOrganizer(boolean organizer) {
        if (!organizer) {
            throw new UnauthorizedResourceException();
        }
    }
}
