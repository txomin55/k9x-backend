package com.k9x.application.notifications.use_case.dto;

import java.util.Map;

/**
 * Read model for a single notification in the user's inbox. {@code type} states the notification kind
 * and {@code metadata} the stored free-form key/value data (ids + display names); the frontend maps
 * type + metadata to the displayed message and URL.
 */
public record NotificationDTO(String id, long timestamp, String type, Map<String, String> metadata, boolean seen) {
}
