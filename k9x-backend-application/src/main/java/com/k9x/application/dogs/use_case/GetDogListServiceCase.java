package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.dto.DogDTO;
import com.k9x.application.dogs.port.GetDogListPersistencePort;
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
