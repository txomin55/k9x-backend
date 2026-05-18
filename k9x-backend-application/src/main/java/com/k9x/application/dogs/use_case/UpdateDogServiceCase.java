package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.command.UpdateDogCommand;
import com.k9x.application.dogs.payload.UpdateDogPersistencePayload;
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

    public void updateDog(String dogId, UpdateDogCommand command, String userId, boolean organizer) {
        Dog dog = getDogPersistencePort.getDog(dogId);
        assertDogValidations(dog, userId, organizer);
        updateDogPersistencePort.updateDog(dogId, new UpdateDogPersistencePayload(command.name(), command.image(),
                command.breed(), command.identity(), command.owner(), command.team(), command.country(), DateUtils.nowUtcMillis()));
    }

    private void assertDogValidations(Dog dog, String userId, boolean organizer) {
        if (dog == null) {
            throw new DogNotFoundException();
        }
        if (dog.deletedAt() != null) {
            throw new DogAlreadyDeletedException();
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
    }
}
