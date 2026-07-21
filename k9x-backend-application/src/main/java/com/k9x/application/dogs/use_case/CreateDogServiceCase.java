package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.exceptions.DogChipAlreadyExistsException;
import com.k9x.application.dogs.port.CreateDogPersistencePort;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.dogs.aggregates.Sex;
import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class CreateDogServiceCase implements TransactionalUseCase {

    private final CreateDogPersistencePort createDogPersistencePort;
    private final GetDogPersistencePort getDogPersistencePort;

    public CreateDogServiceCase(CreateDogPersistencePort createDogPersistencePort, GetDogPersistencePort getDogPersistencePort) {
        this.createDogPersistencePort = createDogPersistencePort;
        this.getDogPersistencePort = getDogPersistencePort;
    }

    public void createDog(String id, String name, String image, String breed, String identity,
                          String owner, String handler, String userId, String team, String country,
                          Sex sex, Integer withersCm, Boolean threeFciGenerationsConfirmed, boolean organizer) {
        assertUserIdMatchesOwnerWhenNoOrganizer(owner, userId, organizer);
        assertChipNotUsedByActiveDog(id);
        assertIdentityNotUsedByActiveDog(identity);
        // If the chip belongs to a soft-deleted dog, the persistence upsert reactivates that row with the
        // new data ("recover"). Only active collisions are rejected above.
        createDogPersistencePort.createDog(id, name, image, breed, identity, owner, handler, userId, team, country,
                sex, withersCm, threeFciGenerationsConfirmed, DateUtils.nowUtcMillis());
    }

    private void assertUserIdMatchesOwnerWhenNoOrganizer(String owner, String userId, boolean organizer) {
        if (!organizer && (owner == null || !owner.equals(userId))) {
            throw new UnauthorizedResourceException();
        }
    }

    private void assertChipNotUsedByActiveDog(String id) {
        if (getDogPersistencePort.getDog(id) != null) {
            throw new DogChipAlreadyExistsException();
        }
    }

    private void assertIdentityNotUsedByActiveDog(String identity) {
        if (getDogPersistencePort.getDogByIdentity(identity) != null) {
            throw new DogChipAlreadyExistsException();
        }
    }
}
