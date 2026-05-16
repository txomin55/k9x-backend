package com.k9x.application.dogs.port;

public interface UpdateDogPersistencePort {

    void updateDog(String id, String name, String image, String breed, String identity,
                   String owner, String team, String country, long lastUpdate);
}
