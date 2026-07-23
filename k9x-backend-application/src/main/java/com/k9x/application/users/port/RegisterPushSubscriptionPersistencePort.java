package com.k9x.application.users.port;

import com.k9x.application.users.port.payload.RegisterPushSubscriptionPersistencePayload;

public interface RegisterPushSubscriptionPersistencePort {

    void registerPushSubscription(RegisterPushSubscriptionPersistencePayload payload);
}
