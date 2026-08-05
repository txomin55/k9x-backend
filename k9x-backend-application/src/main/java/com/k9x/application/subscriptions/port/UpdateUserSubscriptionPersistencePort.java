package com.k9x.application.subscriptions.port;

import com.k9x.application.subscriptions.port.payload.UpdateUserSubscriptionPersistencePayload;

public interface UpdateUserSubscriptionPersistencePort {

    void updateUserSubscription(UpdateUserSubscriptionPersistencePayload payload);
}
