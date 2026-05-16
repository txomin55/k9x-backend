package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.port.CreateDogPersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class CreateDogServiceCase {

    private final CreateDogPersistencePort createDogPersistencePort;

    public CreateDogServiceCase(CreateDogPersistencePort createDogPersistencePort) {
        this.createDogPersistencePort = createDogPersistencePort;
    }

    public void createDog(String id, String name, String image, String breed, String identity,
                          String owner, String userId, String team, String country, boolean organizer) {
        assertUserIdMatchesOwnerWhenNoOrganizer(owner, userId, organizer);
        createDogPersistencePort.createDog(id, name, image, breed, identity, owner, userId, team, country, DateUtils.nowUtcMillis());
    }

    private void assertUserIdMatchesOwnerWhenNoOrganizer(String owner, String userId, boolean organizer) {
        if (!organizer && (owner == null || !owner.equals(userId))) {
            throw new UnauthorizedResourceException();
        }
    }
}
