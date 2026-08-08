package com.k9x.application.dogs.port;

public interface DeleteDogPersistencePort {

    void deleteDog(String identification, long deletedAt);
}
