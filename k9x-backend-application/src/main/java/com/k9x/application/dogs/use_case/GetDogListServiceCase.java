package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.exceptions.OwnerNonProvidedWhenOrganizerException;
import com.k9x.application.dogs.port.GetDogListPersistencePort;
import com.k9x.application.dogs.use_case.dto.DogDTO;
import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.domain.shared.SupportUser;

import java.util.List;

public class GetDogListServiceCase {

    private final GetDogListPersistencePort getDogListPersistencePort;

    public GetDogListServiceCase(GetDogListPersistencePort getDogListPersistencePort) {
        this.getDogListPersistencePort = getDogListPersistencePort;
    }

    public List<DogDTO> getDogs(String userId, boolean organizer, boolean onlyOwned) {

        boolean privileged = organizer || SupportUser.is(userId);
        assertOwnerWhenNoOrganizer(userId, privileged);

        String dogsByOwner = !privileged || onlyOwned ? userId : null;
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
                                dog.getHandler(),
                                dog.getIdentity()
                        )
                )
                .toList();
    }

    private void assertOwnerWhenNoOrganizer(String owner, boolean organizer) {
        if (owner == null && !organizer) {
            throw new OwnerNonProvidedWhenOrganizerException();
        }
    }
}
