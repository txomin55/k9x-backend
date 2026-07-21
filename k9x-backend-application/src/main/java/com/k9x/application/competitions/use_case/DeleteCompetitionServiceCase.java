package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.application.utils.auth.AuthAssertions;
import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;

public class DeleteCompetitionServiceCase implements TransactionalUseCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final SaveCompetitionPersistencePort saveCompetitionPersistencePort;

    public DeleteCompetitionServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                        SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.saveCompetitionPersistencePort = saveCompetitionPersistencePort;
    }

    public void deleteCompetition(String id, String userId, boolean organizer) {
        AuthAssertions.assertOrganizer(organizer, userId);
        CompetitionAggregate competition = CompetitionAggregate.of(getCompetitionPersistencePort.getCompetition(id));
        competition.delete(userId, DateUtils.nowUtcMillis());
        saveCompetitionPersistencePort.save(competition);
    }
}
