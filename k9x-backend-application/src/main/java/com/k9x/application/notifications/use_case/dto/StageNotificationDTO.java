package com.k9x.application.notifications.use_case.dto;

import java.util.List;

/**
 * An organizer's announcement as read back for a stage: when it was sent, which of the stage's events it
 * applies to, and its text.
 */
public record StageNotificationDTO(long timestamp, List<String> eventIds, String content) {
}
