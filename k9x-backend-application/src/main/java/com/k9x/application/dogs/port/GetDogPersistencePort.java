package com.k9x.application.dogs.port;

import com.k9x.domain.dogs.aggregates.Dog;

public interface GetDogPersistencePort {

    Dog getDog(String id);
}
