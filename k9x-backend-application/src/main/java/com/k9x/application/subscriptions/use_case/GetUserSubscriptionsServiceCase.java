package com.k9x.application.subscriptions.use_case;

import com.k9x.application.subscriptions.port.GetUserSubscriptionsPersistencePort;
import com.k9x.application.subscriptions.use_case.dto.UserSubscriptionsDTO;

/**
 * Reads the authenticated user's subscriptions. Read-only, and always scoped by {@code user_id} in the
 * adapter, so a user can only ever read their own.
 */
public class GetUserSubscriptionsServiceCase {

    private final GetUserSubscriptionsPersistencePort getUserSubscriptionsPersistencePort;

    public GetUserSubscriptionsServiceCase(GetUserSubscriptionsPersistencePort getUserSubscriptionsPersistencePort) {
        this.getUserSubscriptionsPersistencePort = getUserSubscriptionsPersistencePort;
    }

    public UserSubscriptionsDTO getUserSubscriptions(String userId) {
        return getUserSubscriptionsPersistencePort.getUserSubscriptions(userId);
    }
}
