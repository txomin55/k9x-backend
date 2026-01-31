package com.k9x.application.dog.port;

import com.k9x.application.dog.command.DogGetCommand;
import com.k9x.application.dog.dto.DogDTO;
import com.k9x.domain.commons.exception.DomainException;
import com.k9x.domain.commons.exception.error.ErrorEnum;
import com.k9x.domain.dog.model.Dog;
import com.k9x.domain.dog.port.GetDogPersistencePort;
import org.springframework.stereotype.Service;

@Service
public class GetDogServiceCase {

    private final GetDogPersistencePort getDogPersistencePort;

    public GetDogServiceCase(GetDogPersistencePort getDogPersistencePort) {
        this.getDogPersistencePort = getDogPersistencePort;
    }

    public DogDTO getDog(DogGetCommand command) {
        Dog dog = getDogPersistencePort.getDog(command.id());

        if (!dog.belongsToSameOwner(command.owner())) {
            throw new DomainException(ErrorEnum.UNAUTHORIZED_RESOURCE_ERROR);
        }

        return new DogDTO(dog.getId(), dog.getName(), dog.getImage(), dog.getOwner());
    }
}
