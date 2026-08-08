package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.exceptions.DogIdentificationAlreadyExistsException;
import com.k9x.application.dogs.exceptions.DogOriginAlreadyExistsException;
import com.k9x.application.dogs.port.CreateDogPersistencePort;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.application.utils.text.Identifiers;
import com.k9x.domain.dogs.aggregates.Sex;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class CreateDogServiceCase implements TransactionalUseCase {

    private final CreateDogPersistencePort createDogPersistencePort;
    private final GetDogPersistencePort getDogPersistencePort;

    public CreateDogServiceCase(CreateDogPersistencePort createDogPersistencePort, GetDogPersistencePort getDogPersistencePort) {
        this.createDogPersistencePort = createDogPersistencePort;
        this.getDogPersistencePort = getDogPersistencePort;
    }

    public void createDog(String identification, String name, String image, String breed, String origin, String license,
                          String owner, String handler, String userId, String team, String country,
                          Sex sex, Integer withersCm, Boolean threeFciGenerationsConfirmed, boolean organizer) {
        assertUserIdMatchesOwnerWhenNoOrganizer(owner, userId, organizer);
        assertIdentificationNotUsedByActiveDog(identification);
        assertOriginNotUsedByActiveDog(origin);
        // If the identification belongs to a soft-deleted dog, the persistence upsert reactivates that row with the
        // new data ("recover"). Only active collisions are rejected above.
        createDogPersistencePort.createDog(identification, name, image, breed, origin, license, owner, handler, userId, team, country,
                sex, withersCm, threeFciGenerationsConfirmed, DateUtils.nowUtcMillis());
    }

    private void assertUserIdMatchesOwnerWhenNoOrganizer(String owner, String userId, boolean organizer) {
        if (!organizer && (owner == null || !owner.equals(userId))) {
            throw new UnauthorizedResourceException();
        }
    }

    private void assertIdentificationNotUsedByActiveDog(String identification) {
        if (Identifiers.isPresent(identification) && getDogPersistencePort.getDog(identification) != null) {
            throw new DogIdentificationAlreadyExistsException();
        }
    }

    private void assertOriginNotUsedByActiveDog(String origin) {
        if (Identifiers.isPresent(origin) && getDogPersistencePort.getDogByOrigin(origin) != null) {
            throw new DogOriginAlreadyExistsException();
        }
    }
}
