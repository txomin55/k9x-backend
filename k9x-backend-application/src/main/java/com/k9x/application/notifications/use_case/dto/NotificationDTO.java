package com.k9x.application.notifications.use_case.dto;

/**
 * Read model for a single notification in the user's inbox. {@code text} carries the raw serialized
 * metadata as stored (a JSON string); the frontend parses it to render the displayed message.
 */
public record NotificationDTO(String id, long timestamp, String text, boolean seen) {
}
