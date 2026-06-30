package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.exceptions.DogAlreadyDeletedException;
import com.k9x.application.dogs.exceptions.DogNotFoundException;
import com.k9x.application.dogs.port.DeleteDogPersistencePort;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.dogs.aggregates.Dog;
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
        assertDogValidations(dog, userId, organizer);
        deleteDogPersistencePort.deleteDog(dogId, DateUtils.nowUtcMillis());
    }

    private void assertDogValidations(Dog dog, String userId, boolean organizer) {
        if (dog == null) {
            throw new DogNotFoundException();
        }
        if (dog.deletedAt() != null) {
            throw new DogAlreadyDeletedException();
        }
        if (dog.owner() != null) {
            // When the dog has an owner, only the owner can delete it (not the organizer nor the creator).
            if (!dog.owner().equals(userId)) {
                throw new UnauthorizedResourceException();
            }
        } else {
            // Ownerless dogs can only be deleted by the organizer that created them.
            if (!organizer) {
                throw new UnauthorizedResourceException();
            }
            if (!dog.creator().equals(userId)) {
                throw new UnauthorizedResourceException();
            }
        }
    }
}
