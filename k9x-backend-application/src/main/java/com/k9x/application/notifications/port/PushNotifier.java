package com.k9x.application.notifications.port;

import com.k9x.application.notifications.valueobjects.PushNotification;

/**
 * Utility injected into service cases to notify a user. Implementations deliver best-effort and
 * asynchronously: both methods return immediately and never throw, so a service case can call them
 * from inside its transaction without blocking the response or risking a rollback on delivery failure.
 */
public interface PushNotifier {

    /**
     * Records the notification in the recipient's inbox <em>and</em> pushes it. Both run best-effort after
     * the caller's transaction commits, so the inbox row is not covered by that transaction.
     */
    void notify(String recipientUserId, PushNotification notification);

    /**
     * Pushes the notification without recording it, for callers that write the inbox row themselves inside
     * their own transaction and only want the delivery deferred until after the commit.
     */
    void deliver(String recipientUserId, PushNotification notification);
}
