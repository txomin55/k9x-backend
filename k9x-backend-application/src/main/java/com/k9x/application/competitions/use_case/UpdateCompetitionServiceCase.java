package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.port.payload.UpdateCompetitionPersistencePayload;
import com.k9x.application.competitions.use_case.command.UpdateCompetitionCommand;
import com.k9x.application.competitions.use_case.dto.Coordinates;
import com.k9x.application.competitions.exceptions.CompetitionAlreadyDeletedException;
import com.k9x.application.competitions.exceptions.CompetitionNotFoundException;
import com.k9x.application.competitions.port.GeoCoordinatesPort;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.UpdateCompetitionPersistencePort;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class UpdateCompetitionServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final GeoCoordinatesPort geoCoordinatesPort;
    private final UpdateCompetitionPersistencePort updateCompetitionPersistencePort;

    public UpdateCompetitionServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                        GeoCoordinatesPort geoCoordinatesPort,
                                        UpdateCompetitionPersistencePort updateCompetitionPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.geoCoordinatesPort = geoCoordinatesPort;
        this.updateCompetitionPersistencePort = updateCompetitionPersistencePort;
    }

    public void updateCompetition(String id, UpdateCompetitionCommand command, String userId, boolean organizer) {
        assertOrganizer(organizer);
        Competition competition = getCompetitionPersistencePort.getCompetition(id);
        assertCompetitionValidations(competition, userId);
        Coordinates coordinates = geoCoordinatesPort.getCoordinates(command.address());
        updateCompetitionPersistencePort.updateCompetition(id, UpdateCompetitionPersistencePayload.from(command, coordinates));
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
