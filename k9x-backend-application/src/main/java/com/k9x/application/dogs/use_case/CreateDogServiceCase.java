package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.port.CreateDogPersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.dogs.aggregates.Sex;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.domain.shared.SupportUser;

public class CreateDogServiceCase {

    private final CreateDogPersistencePort createDogPersistencePort;

    public CreateDogServiceCase(CreateDogPersistencePort createDogPersistencePort) {
        this.createDogPersistencePort = createDogPersistencePort;
    }

    public void createDog(String id, String name, String image, String breed, String identity,
                          String owner, String handler, String userId, String team, String country,
                          Sex sex, Integer withersCm, boolean organizer) {
        assertUserIdMatchesOwnerWhenNoOrganizer(owner, userId, organizer);
        createDogPersistencePort.createDog(id, name, image, breed, identity, owner, handler, userId, team, country, sex, withersCm, DateUtils.nowUtcMillis());
    }

    private void assertUserIdMatchesOwnerWhenNoOrganizer(String owner, String userId, boolean organizer) {
        if (SupportUser.is(userId)) {
            return;
        }
        if (!organizer && (owner == null || !owner.equals(userId))) {
            throw new UnauthorizedResourceException();
        }
    }
}
