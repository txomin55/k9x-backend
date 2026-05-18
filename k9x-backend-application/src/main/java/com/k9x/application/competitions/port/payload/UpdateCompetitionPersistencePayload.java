package com.k9x.application.competitions.port.payload;

import com.k9x.application.competitions.use_case.command.UpdateCompetitionCommand;
import com.k9x.application.competitions.use_case.dto.Coordinates;
import com.k9x.application.utils.date.DateUtils;

public record UpdateCompetitionPersistencePayload(String name, String description, String country,
                                                  String address, Double coordAlt, Double coordLong,
                                                  long lastUpdate) {

    public static UpdateCompetitionPersistencePayload from(UpdateCompetitionCommand command, Coordinates coordinates) {
        return new UpdateCompetitionPersistencePayload(
                command.name(), command.description(), command.country(), command.address(),
                coordinates.coordAlt(), coordinates.coordLong(), DateUtils.nowUtcMillis());
    }
}
