package com.k9x.domain.dog.port;

import com.k9x.domain.dog.model.Dog;

public interface GetDogPersistencePort {

    Dog getDog(String id);
}
