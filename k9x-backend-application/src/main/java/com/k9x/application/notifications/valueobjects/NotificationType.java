package com.k9x.application.notifications.valueobjects;

/**
 * Domain-level notification kinds. The backend only states *what happened*; the frontend maps each type
 * (plus its metadata) to the displayed text and the URL to open on click, so routing stays a frontend
 * concern.
 */
public enum NotificationType {
    NEW_ENROLL
}
