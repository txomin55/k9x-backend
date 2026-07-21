package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.port.GeoCoordinatesPort;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.competitions.use_case.command.UpdateCompetitionCommand;
import com.k9x.application.competitions.use_case.dto.Coordinates;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.application.utils.auth.AuthAssertions;
import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.domain.competitions.commands.CompetitionUpdateData;

public class UpdateCompetitionServiceCase implements TransactionalUseCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final GeoCoordinatesPort geoCoordinatesPort;
    private final SaveCompetitionPersistencePort saveCompetitionPersistencePort;

    public UpdateCompetitionServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                        GeoCoordinatesPort geoCoordinatesPort,
                                        SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.geoCoordinatesPort = geoCoordinatesPort;
        this.saveCompetitionPersistencePort = saveCompetitionPersistencePort;
    }

    public void updateCompetition(String id, UpdateCompetitionCommand command, String userId, boolean organizer) {
        AuthAssertions.assertOrganizer(organizer, userId);
        Coordinates coordinates = geoCoordinatesPort.getCoordinates(command.address());
        CompetitionAggregate competition = CompetitionAggregate.of(getCompetitionPersistencePort.getCompetition(id));
        competition.update(new CompetitionUpdateData(command.name(), command.description(), command.country(),
                command.address(), coordinates.coordAlt(), coordinates.coordLong()), userId, DateUtils.nowUtcMillis());
        saveCompetitionPersistencePort.save(competition);
    }
}
