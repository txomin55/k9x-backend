package com.k9x.application.dogs.port;

import com.k9x.domain.aggregates.dogs.Dog;

public interface GetDogPersistencePort {

    Dog getDog(String id);
}
