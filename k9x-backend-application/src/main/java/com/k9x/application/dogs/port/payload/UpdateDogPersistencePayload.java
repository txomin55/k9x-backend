package com.k9x.application.dogs.port.payload;

import com.k9x.application.dogs.use_case.command.UpdateDogCommand;
import com.k9x.application.utils.date.DateUtils;

public record UpdateDogPersistencePayload(String name, String image, String breed, String identity,
                                          String owner, String team, String country, long lastUpdate) {

    public static UpdateDogPersistencePayload from(UpdateDogCommand command) {
        return new UpdateDogPersistencePayload(
                command.name(), command.image(), command.breed(), command.identity(),
                command.owner(), command.team(), command.country(), DateUtils.nowUtcMillis());
    }
}
