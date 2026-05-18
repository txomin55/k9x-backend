package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.exceptions.CompetitionAlreadyDeletedException;
import com.k9x.application.competitions.exceptions.CompetitionNotFoundException;
import com.k9x.application.competitions.port.DeleteCompetitionPersistencePort;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class DeleteCompetitionServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final DeleteCompetitionPersistencePort deleteCompetitionPersistencePort;

    public DeleteCompetitionServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                        DeleteCompetitionPersistencePort deleteCompetitionPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.deleteCompetitionPersistencePort = deleteCompetitionPersistencePort;
    }

    public void deleteCompetition(String id, String userId, boolean organizer) {
        assertOrganizer(organizer);
        Competition competition = getCompetitionPersistencePort.getCompetition(id);
        assertCompetitionValidations(competition, userId);
        deleteCompetitionPersistencePort.deleteCompetition(id, DateUtils.nowUtcMillis());
    }

    private void assertOrganizer(boolean organizer) {
        if (!organizer) {
            throw new UnauthorizedResourceException();
        }
    }

    private void assertCompetitionValidations(Competition competition, String userId) {
        if (competition == null) {
            throw new CompetitionNotFoundException();
        }
        if (competition.deletedAt() != null) {
            throw new CompetitionAlreadyDeletedException();
        }
        if (!competition.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
    }
}
