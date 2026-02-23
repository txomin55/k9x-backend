package com.k9x.application.dog.action;

import com.k9x.application.dog.dto.DogDTO;
import com.k9x.application.dog.port.GetDogListPersistencePort;
import com.k9x.domain.dog.model.Dog;

import java.util.List;

public class GetDogListServiceCase {

    private final GetDogListPersistencePort getDogListPersistencePort;

    public GetDogListServiceCase(GetDogListPersistencePort getDogListPersistencePort) {
        this.getDogListPersistencePort = getDogListPersistencePort;
    }

    public List<DogDTO> getDogs(String owner) {
        List<Dog> dogs = getDogListPersistencePort.getDogs(owner);

        return dogs.stream().map(dog -> new DogDTO(dog.getId(), dog.getName(), dog.getImage(), dog.getOwner()))
                .toList();
    }
}
