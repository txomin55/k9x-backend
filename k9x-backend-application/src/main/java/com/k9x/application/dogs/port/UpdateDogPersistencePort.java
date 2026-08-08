package com.k9x.application.dogs.port;

import com.k9x.application.dogs.port.payload.UpdateDogPersistencePayload;

public interface UpdateDogPersistencePort {

    void updateDog(String identification, UpdateDogPersistencePayload payload);
}
