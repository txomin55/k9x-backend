package com.k9x.application.dogs.port;

public interface CreateDogPersistencePort {

    void createDog(String id, String name, String image, String breed, String identity,
                   String owner, String creator, String team, String country, long createdAt);
}
