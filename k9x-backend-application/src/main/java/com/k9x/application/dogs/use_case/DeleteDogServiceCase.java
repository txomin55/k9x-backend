package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.exceptions.DogAlreadyDeletedException;
import com.k9x.application.dogs.exceptions.DogNotFoundException;
import com.k9x.application.dogs.port.DeleteDogPersistencePort;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.dogs.Dog;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class DeleteDogServiceCase {

    private final GetDogPersistencePort getDogPersistencePort;
    private final DeleteDogPersistencePort deleteDogPersistencePort;

    public DeleteDogServiceCase(GetDogPersistencePort getDogPersistencePort,
                                DeleteDogPersistencePort deleteDogPersistencePort) {
        this.getDogPersistencePort = getDogPersistencePort;
        this.deleteDogPersistencePort = deleteDogPersistencePort;
    }

    public void deleteDog(String dogId, String userId, boolean organizer) {
        Dog dog = getDogPersistencePort.getDog(dogId);
        if (dog == null) {
            throw new DogNotFoundException();
        }

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

        if (dog.deletedAt() != null) {
            throw new DogAlreadyDeletedException();
        }

        deleteDogPersistencePort.deleteDog(dogId, DateUtils.nowUtcMillis());
    }
}
