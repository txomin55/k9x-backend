package com.k9x.application.subscriptions.port;

import com.k9x.application.subscriptions.use_case.dto.UserSubscriptionsDTO;

public interface GetUserSubscriptionsPersistencePort {

    UserSubscriptionsDTO getUserSubscriptions(String userId);
}
