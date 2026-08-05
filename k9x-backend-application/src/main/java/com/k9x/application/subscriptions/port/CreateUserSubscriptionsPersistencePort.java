package com.k9x.application.subscriptions.port;

public interface CreateUserSubscriptionsPersistencePort {

    /**
     * Creates the user's empty subscriptions record. Idempotent: called on the user's first login, and
     * again before a toggle so users created before this feature existed also get their record.
     */
    void createUserSubscriptions(String userId);
}
