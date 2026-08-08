package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.exceptions.OwnerNonProvidedWhenOrganizerException;
import com.k9x.application.dogs.port.GetDogListPersistencePort;
import com.k9x.application.dogs.use_case.dto.DogDTO;
import com.k9x.domain.dogs.aggregates.Dog;

import java.util.List;

public class GetDogListServiceCase {

    private final GetDogListPersistencePort getDogListPersistencePort;

    public GetDogListServiceCase(GetDogListPersistencePort getDogListPersistencePort) {
        this.getDogListPersistencePort = getDogListPersistencePort;
    }

    public List<DogDTO> getDogs(String userId, boolean organizer, boolean owned, boolean created) {

        assertOwnerWhenNoOrganizer(userId, organizer);

        boolean filterByOwner = owned;
        boolean filterByCreator = created;
        // A non-organizer may only ever list their own dogs (owned or created).
        if (!organizer && !owned && !created) {
            filterByOwner = true;
            filterByCreator = true;
        }

        String ownerFilter = filterByOwner ? userId : null;
        String creatorFilter = filterByCreator ? userId : null;
        List<Dog> dogs = getDogListPersistencePort.getDogs(ownerFilter, creatorFilter);

        return dogs.stream()
                .map(dog -> new DogDTO(
                                dog.getIdentification(),
                                dog.getName(),
                                dog.getImage(),
                                userId.equals(dog.getOwner()) || (dog.getOwner() == null && userId.equals(dog.getCreator())),
                                dog.getCountry(),
                                dog.getTeam(),
                                dog.getOwner(),
                                dog.getHandler(),
                                dog.getOrigin(),
                                dog.getLicense(),
                                dog.breed(),
                                dog.getSex(),
                                dog.getWithersCm(),
                                dog.getThreeFciGenerationsConfirmed()
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
