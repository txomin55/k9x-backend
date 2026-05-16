package com.k9x.application.dogs.port;

public interface DeleteDogPersistencePort {

    void deleteDog(String id, long deletedAt);
}
