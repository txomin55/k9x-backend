package com.k9x.application.users.port;

public interface DeletePushSubscriptionPersistencePort {

    /**
     * Prunes a subscription the push service has reported as gone. No owner is known at that point —
     * the push service only identifies the subscription by its endpoint.
     */
    void deleteByEndpoint(String endpoint);

    /**
     * Removes a subscription on the user's request. Scoped to the owner so an endpoint leaked or
     * guessed by a third party cannot be used to silence someone else's device.
     */
    void deleteByEndpointAndUserId(String endpoint, String userId);
}
