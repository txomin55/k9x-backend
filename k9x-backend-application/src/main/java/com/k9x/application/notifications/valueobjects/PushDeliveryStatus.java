package com.k9x.application.notifications.valueobjects;

/**
 * Outcome of a single web-push delivery attempt.
 *
 * <p>{@code EXPIRED} maps to the push service's 404/410 Gone responses: the subscription no longer
 * exists on the client and must be dropped from storage. {@code FAILED} covers any other transport or
 * server error and is non-fatal — delivery to the remaining subscriptions continues.
 */
public enum PushDeliveryStatus {
    DELIVERED, EXPIRED, FAILED
}
