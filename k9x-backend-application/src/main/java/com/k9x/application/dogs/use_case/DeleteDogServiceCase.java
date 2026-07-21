package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.port.DeleteDogPersistencePort;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.domain.dogs.aggregates.Dog;

public class DeleteDogServiceCase implements TransactionalUseCase {

    private final GetDogPersistencePort getDogPersistencePort;
    private final DeleteDogPersistencePort deleteDogPersistencePort;

    public DeleteDogServiceCase(GetDogPersistencePort getDogPersistencePort,
                                DeleteDogPersistencePort deleteDogPersistencePort) {
        this.getDogPersistencePort = getDogPersistencePort;
        this.deleteDogPersistencePort = deleteDogPersistencePort;
    }

    public void deleteDog(String dogId, String userId, boolean organizer) {
        Dog dog = getDogPersistencePort.getDog(dogId);
        DogGuards.assertMutableBy(dog, userId, organizer);
        deleteDogPersistencePort.deleteDog(dogId, DateUtils.nowUtcMillis());
    }
}
