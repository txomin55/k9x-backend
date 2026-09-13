package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.exceptions.DogNotFoundException;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.dogs.use_case.dto.PublicDogDetailDTO;
import com.k9x.domain.dogs.aggregates.Dog;

/**
 * The public detail of one dog. It asserts nothing about the caller: the endpoint sits outside
 * {@code /secured/}, so there may well be no user behind the request, and every active dog is readable by
 * everyone.
 *
 * <p>The persistence port already resolves active dogs only, so a deleted dog is indistinguishable from one
 * that never existed and both answer "not found" — deliberately: whether a dog was deleted is not something
 * an anonymous reader gets to learn.
 */
public class GetPublicDogServiceCase {

    private final GetDogPersistencePort getDogPersistencePort;

    public GetPublicDogServiceCase(GetDogPersistencePort getDogPersistencePort) {
        this.getDogPersistencePort = getDogPersistencePort;
    }

    public PublicDogDetailDTO getDog(String identification) {
        Dog dog = getDogPersistencePort.getDog(identification);
        if (dog == null) {
            throw new DogNotFoundException();
        }
        return PublicDogDetailDTO.from(dog);
    }
}
