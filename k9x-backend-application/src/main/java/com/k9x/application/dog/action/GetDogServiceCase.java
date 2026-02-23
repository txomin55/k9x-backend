package com.k9x.application.dog.action;

import com.k9x.application.dog.command.DogGetCommand;
import com.k9x.application.dog.dto.DogDTO;
import com.k9x.application.dog.port.GetDogPersistencePort;
import com.k9x.domain.commons.exception.UnauthorizedResourceException;
import com.k9x.domain.dog.model.Dog;

public class GetDogServiceCase {

    private final GetDogPersistencePort getDogPersistencePort;

    public GetDogServiceCase(GetDogPersistencePort getDogPersistencePort) {
        this.getDogPersistencePort = getDogPersistencePort;
    }

    public DogDTO getDog(DogGetCommand command) {
        Dog dog = getDogPersistencePort.getDog(command.id());

        if (!dog.belongsToSameOwner(command.owner())) {
            throw new UnauthorizedResourceException();
        }

        return new DogDTO(dog.getId(), dog.getName(), dog.getImage(), dog.getOwner());
    }
}
