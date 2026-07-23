package com.k9x.application.notifications.port;

import com.k9x.application.notifications.valueobjects.PushNotification;

/**
 * Utility injected into service cases to notify a user. Implementations deliver best-effort and
 * asynchronously: {@link #notify} returns immediately and never throws, so a service case can call it
 * from inside its transaction without blocking the response or risking a rollback on delivery failure.
 */
public interface PushNotifier {

    void notify(String recipientUserId, PushNotification notification);
}
