package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.exceptions.DogAlreadyDeletedException;
import com.k9x.application.dogs.exceptions.DogNotFoundException;
import com.k9x.application.utils.text.Identifiers;
import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

/**
 * Shared write guards for dogs, so the (identical) validation in update/delete lives in one place.
 * A dog that has an owner can only be mutated by that owner; an ownerless dog only by the organizer that
 * created it. The existence guard (not found) always applies.
 * "Ownerless" means blank, not just {@code null}: the clients send {@code owner: ""} when the field is left
 * empty, so an empty string must not be read as an owner nobody can ever match.
 */
public final class DogGuards {

    private DogGuards() {}

    public static void assertMutableBy(Dog dog, String userId, boolean organizer) {
        if (dog == null) {
            throw new DogNotFoundException();
        }
        if (dog.deletedAt() != null) {
            throw new DogAlreadyDeletedException();
        }
        if (Identifiers.isPresent(dog.owner())) {
            if (!dog.owner().equals(userId)) {
                throw new UnauthorizedResourceException();
            }
        } else {
            if (!organizer) {
                throw new UnauthorizedResourceException();
            }
            if (!dog.creator().equals(userId)) {
                throw new UnauthorizedResourceException();
            }
        }
    }
}
