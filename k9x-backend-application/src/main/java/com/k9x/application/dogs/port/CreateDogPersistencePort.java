package com.k9x.application.dogs.port;

import com.k9x.domain.dogs.aggregates.Sex;

public interface CreateDogPersistencePort {

    void createDog(String id, String name, String image, String breed, String identity,
                   String owner, String handler, String creator, String team, String country,
                   Sex sex, Integer withersCm, long createdAt);
}
