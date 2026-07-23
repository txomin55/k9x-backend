package com.k9x.application.users.port;

public interface DeletePushSubscriptionPersistencePort {

    void deleteByEndpoint(String endpoint);
}
