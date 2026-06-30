package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.exceptions.DogAlreadyDeletedException;
import com.k9x.application.dogs.exceptions.DogNotFoundException;
import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.domain.shared.SupportUser;

/**
 * Shared write guards for dogs, so the (identical) validation in update/delete lives in one place.
 * A dog that has an owner can only be mutated by that owner; an ownerless dog only by the organizer that
 * created it. The {@link SupportUser support superuser} bypasses every check. The existence guard (not
 * found) always applies.
 */
public final class DogGuards {

    private DogGuards() {}

    public static void assertMutableBy(Dog dog, String userId, boolean organizer) {
        if (dog == null) {
            throw new DogNotFoundException();
        }
        if (SupportUser.is(userId)) {
            return;
        }
        if (dog.deletedAt() != null) {
            throw new DogAlreadyDeletedException();
        }
        if (dog.owner() != null) {
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
