package com.k9x.application.subscriptions.use_case.dto;

import java.util.List;

/**
 * The authenticated user's subscriptions, one list per {@link com.k9x.domain.subscriptions.SubscriptionKind}.
 */
public record UserSubscriptionsDTO(List<String> eventIds) {

    public static UserSubscriptionsDTO empty() {
        return new UserSubscriptionsDTO(List.of());
    }
}
