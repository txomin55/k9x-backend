package com.k9x.domain.subscriptions;

import com.k9x.domain.subscriptions.exceptions.SubscriptionKindNotSupportedException;

import java.util.Locale;

/**
 * Kinds of resource a user can subscribe to. The subscription endpoint is generic: the request states
 * <em>what</em> is being subscribed ({@code kind}) plus its id, and each kind maps to its own storage
 * (today {@code EVENT} maps to the {@code event_ids} list of {@code k9x.user_subscriptions}). Adding a
 * new kind means adding a constant here, a column, and its mapping in the persistence adapter — the
 * REST contract does not change.
 */
public enum SubscriptionKind {
    EVENT;

    public static SubscriptionKind of(String value) {
        if (value == null || value.isBlank()) {
            throw new SubscriptionKindNotSupportedException(String.valueOf(value));
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new SubscriptionKindNotSupportedException(value);
        }
    }
}
