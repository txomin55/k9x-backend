package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.utils.auth.AuthAssertions;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;

public class CreateCompetitionServiceCase implements TransactionalUseCase {

    private final SaveCompetitionPersistencePort saveCompetitionPersistencePort;

    public CreateCompetitionServiceCase(SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        this.saveCompetitionPersistencePort = saveCompetitionPersistencePort;
    }

    public void createCompetition(String id, String name, String userId, boolean organizer) {
        AuthAssertions.assertOrganizer(organizer, userId);
        CompetitionAggregate competition = CompetitionAggregate.createNew(id, name, userId, DateUtils.nowUtcMillis());
        saveCompetitionPersistencePort.save(competition);
    }
}
