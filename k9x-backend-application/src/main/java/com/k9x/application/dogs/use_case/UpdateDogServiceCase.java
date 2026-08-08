package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.exceptions.DogOriginAlreadyExistsException;
import com.k9x.application.dogs.port.payload.UpdateDogPersistencePayload;
import com.k9x.application.dogs.use_case.command.UpdateDogCommand;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.dogs.port.UpdateDogPersistencePort;
import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.application.utils.text.Identifiers;
import com.k9x.domain.dogs.aggregates.Dog;

public class UpdateDogServiceCase implements TransactionalUseCase {

    private final GetDogPersistencePort getDogPersistencePort;
    private final UpdateDogPersistencePort updateDogPersistencePort;

    public UpdateDogServiceCase(GetDogPersistencePort getDogPersistencePort,
                                UpdateDogPersistencePort updateDogPersistencePort) {
        this.getDogPersistencePort = getDogPersistencePort;
        this.updateDogPersistencePort = updateDogPersistencePort;
    }

    public void updateDog(String dogIdentification, UpdateDogCommand command, String userId, boolean organizer) {
        Dog dog = getDogPersistencePort.getDog(dogIdentification);
        DogGuards.assertMutableBy(dog, userId, organizer);
        assertOriginNotUsedByAnotherDog(dogIdentification, command.origin());
        updateDogPersistencePort.updateDog(dogIdentification, UpdateDogPersistencePayload.from(command));
    }

    private void assertOriginNotUsedByAnotherDog(String dogIdentification, String origin) {
        if (Identifiers.isBlank(origin)) {
            return;
        }
        Dog existing = getDogPersistencePort.getDogByOrigin(origin);
        if (existing != null && !existing.identification().equals(dogIdentification)) {
            throw new DogOriginAlreadyExistsException();
        }
    }
}
