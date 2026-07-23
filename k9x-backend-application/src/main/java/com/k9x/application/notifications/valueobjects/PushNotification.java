package com.k9x.application.notifications.valueobjects;

import java.util.Map;

/**
 * Payload delivered to a client: a {@link NotificationType} plus free-form string metadata (ids, etc.).
 * Carries no display text and no URL on purpose — the frontend derives both from the type and metadata.
 */
public record PushNotification(NotificationType type, Map<String, String> metadata) {
}
