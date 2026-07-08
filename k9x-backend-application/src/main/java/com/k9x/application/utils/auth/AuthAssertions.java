package com.k9x.application.utils.auth;

import com.k9x.domain.exceptions.UnauthorizedResourceException;

/**
 * Shared authorization guards for the service-case layer, so cross-cutting rules live in a single place
 * instead of being duplicated in every service case.
 */
public final class AuthAssertions {

    private AuthAssertions() {}

    /**
     * Organizer-only gate.
     */
    public static void assertOrganizer(boolean organizer, String userId) {
        if (!organizer) {
            throw new UnauthorizedResourceException();
        }
    }
}
