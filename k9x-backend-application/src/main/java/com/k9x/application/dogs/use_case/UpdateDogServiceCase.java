package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.exceptions.DogChipAlreadyExistsException;
import com.k9x.application.dogs.port.payload.UpdateDogPersistencePayload;
import com.k9x.application.dogs.use_case.command.UpdateDogCommand;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.dogs.port.UpdateDogPersistencePort;
import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.domain.dogs.aggregates.Dog;

public class UpdateDogServiceCase implements TransactionalUseCase {

    private final GetDogPersistencePort getDogPersistencePort;
    private final UpdateDogPersistencePort updateDogPersistencePort;

    public UpdateDogServiceCase(GetDogPersistencePort getDogPersistencePort,
                                UpdateDogPersistencePort updateDogPersistencePort) {
        this.getDogPersistencePort = getDogPersistencePort;
        this.updateDogPersistencePort = updateDogPersistencePort;
    }

    public void updateDog(String dogId, UpdateDogCommand command, String userId, boolean organizer) {
        Dog dog = getDogPersistencePort.getDog(dogId);
        DogGuards.assertMutableBy(dog, userId, organizer);
        assertIdentityNotUsedByAnotherDog(dogId, command.identity());
        updateDogPersistencePort.updateDog(dogId, UpdateDogPersistencePayload.from(command));
    }

    private void assertIdentityNotUsedByAnotherDog(String dogId, String identity) {
        Dog existing = getDogPersistencePort.getDogByIdentity(identity);
        if (existing != null && !existing.id().equals(dogId)) {
            throw new DogChipAlreadyExistsException();
        }
    }
}
