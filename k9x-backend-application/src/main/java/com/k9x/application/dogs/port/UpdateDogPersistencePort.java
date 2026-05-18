package com.k9x.application.dogs.port;

import com.k9x.application.dogs.payload.UpdateDogPersistencePayload;

public interface UpdateDogPersistencePort {

    void updateDog(String id, UpdateDogPersistencePayload payload);
}
