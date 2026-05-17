package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.exceptions.DogAlreadyDeletedException;
import com.k9x.application.dogs.exceptions.DogNotFoundException;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.dogs.port.UpdateDogPersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.dogs.Dog;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class UpdateDogServiceCase {

    private final GetDogPersistencePort getDogPersistencePort;
    private final UpdateDogPersistencePort updateDogPersistencePort;

    public UpdateDogServiceCase(GetDogPersistencePort getDogPersistencePort,
                                UpdateDogPersistencePort updateDogPersistencePort) {
        this.getDogPersistencePort = getDogPersistencePort;
        this.updateDogPersistencePort = updateDogPersistencePort;
    }

    public void updateDog(String dogId, String name, String image, String breed, String identity,
                          String owner, String userId, String team, String country, boolean organizer) {
        Dog dog = getDogPersistencePort.getDog(dogId);
        assertDogExists(dog);
        assertDogNotDeleted(dog);
        assertUserCanUpdateDog(dog, userId, organizer);
        updateDogPersistencePort.updateDog(dogId, name, image, breed, identity, owner, team, country, DateUtils.nowUtcMillis());
    }

    private void assertDogExists(Dog dog) {
        if (dog == null) {
            throw new DogNotFoundException();
        }
    }

    private void assertDogNotDeleted(Dog dog) {
        if (dog.deletedAt() != null) {
            throw new DogAlreadyDeletedException();
        }
    }

    private void assertUserCanUpdateDog(Dog dog, String userId, boolean organizer) {
        if (dog.owner() != null) {
            if (!dog.owner().equals(userId)) {
                throw new UnauthorizedResourceException();
            }
        } else {
            if (!organizer) {
                throw new UnauthorizedResourceException();
            }
            if (!dog.creator().equals(userId)) {
                throw new UnauthorizedResourceException();
            }
        }
    }
}
