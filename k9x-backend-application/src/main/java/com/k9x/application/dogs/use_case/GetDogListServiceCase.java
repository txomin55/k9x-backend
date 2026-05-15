package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.dto.DogDTO;
import com.k9x.application.dogs.exceptions.OwnerNonProvidedWhenOrganizer;
import com.k9x.application.dogs.port.GetDogListPersistencePort;
import com.k9x.domain.aggregates.dogs.Dog;

import java.util.List;

public class GetDogListServiceCase {

    private final GetDogListPersistencePort getDogListPersistencePort;

    public GetDogListServiceCase(GetDogListPersistencePort getDogListPersistencePort) {
        this.getDogListPersistencePort = getDogListPersistencePort;
    }

    public List<DogDTO> getDogs(String userId, boolean organizer, boolean onlyOwned) {

        assertOwnerWhenNoOrganizer(userId, organizer);

        String dogsByOwner = !organizer || onlyOwned ? userId : null;
        List<Dog> dogs = getDogListPersistencePort.getDogs(dogsByOwner);

        return dogs.stream()
                .map(dog -> new DogDTO(
                                dog.getId(),
                                dog.getName(),
                                dog.getImage(),
                                userId.equals(dog.getOwner()),
                                dog.getCreator(),
                                dog.getCountry(),
                                dog.getTeam(),
                                dog.getOwner(),
                                dog.getIdentity()
                        )
                )
                .toList();
    }

    private void assertOwnerWhenNoOrganizer(String owner, boolean organizer) {
        if (owner == null && !organizer) {
            throw new OwnerNonProvidedWhenOrganizer();
        }
    }
}
