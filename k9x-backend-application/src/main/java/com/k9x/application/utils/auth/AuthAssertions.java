package com.k9x.application.utils.auth;

import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.domain.shared.SupportUser;

/**
 * Shared authorization guards for the service-case layer, so cross-cutting rules (such as the
 * {@link SupportUser} superuser bypass) live in a single place instead of being duplicated in every
 * service case.
 */
public final class AuthAssertions {

    private AuthAssertions() {}

    /**
     * Organizer-only gate. The {@link SupportUser support superuser} is always allowed.
     */
    public static void assertOrganizer(boolean organizer, String userId) {
        if (!organizer && !SupportUser.is(userId)) {
            throw new UnauthorizedResourceException();
        }
    }
}
