package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.exceptions.OwnerNonProvidedWhenOrganizerException;
import com.k9x.application.dogs.port.GetDogListPersistencePort;
import com.k9x.application.dogs.port.payload.DogListFilter;
import com.k9x.application.dogs.port.payload.DogListPage;
import com.k9x.application.dogs.use_case.command.GetDogListCommand;
import com.k9x.application.dogs.use_case.dto.DogDTO;
import com.k9x.application.dogs.use_case.dto.DogListDTO;

import java.util.List;

public class GetDogListServiceCase {

    private final GetDogListPersistencePort getDogListPersistencePort;

    public GetDogListServiceCase(GetDogListPersistencePort getDogListPersistencePort) {
        this.getDogListPersistencePort = getDogListPersistencePort;
    }

    public DogListDTO getDogs(String userId, boolean organizer, GetDogListCommand command) {

        assertOwnerWhenNoOrganizer(userId, organizer);

        boolean filterByOwner = command.owned();
        boolean filterByCreator = command.created();
        // A non-organizer may only ever list their own dogs (owned or created).
        if (!organizer && !command.owned() && !command.created()) {
            filterByOwner = true;
            filterByCreator = true;
        }

        String ownerFilter = filterByOwner ? userId : null;
        String creatorFilter = filterByCreator ? userId : null;
        DogListFilter filter = DogListFilter.from(ownerFilter, creatorFilter, command);
        DogListPage page = getDogListPersistencePort.getDogs(filter);

        List<DogDTO> items = page.dogs().stream()
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

        return DogListDTO.of(items, filter, page.total());
    }

    private void assertOwnerWhenNoOrganizer(String owner, boolean organizer) {
        if (owner == null && !organizer) {
            throw new OwnerNonProvidedWhenOrganizerException();
        }
    }
}
