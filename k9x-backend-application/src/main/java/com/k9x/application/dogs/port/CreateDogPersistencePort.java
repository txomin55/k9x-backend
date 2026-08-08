package com.k9x.application.dogs.port;

import com.k9x.domain.dogs.aggregates.Sex;

public interface CreateDogPersistencePort {

    void createDog(String identification, String name, String image, String breed, String origin, String license,
                   String owner, String handler, String creator, String team, String country,
                   Sex sex, Integer withersCm, Boolean threeFciGenerationsConfirmed, long createdAt);
}
