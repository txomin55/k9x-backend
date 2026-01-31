package com.k9x.domain.dog.port;

import com.k9x.domain.dog.model.Dog;

import java.util.List;

public interface GetDogListPersistencePort {

    List<Dog> getDogs(String owner);
}
