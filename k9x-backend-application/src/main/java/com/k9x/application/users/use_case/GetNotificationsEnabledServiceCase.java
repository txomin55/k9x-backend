package com.k9x.application.users.use_case;

import com.k9x.application.users.port.GetUserNotificationsEnabledPersistencePort;

/**
 * Tells the client whether the account receives push notifications, so its toggle renders the account's
 * state rather than what the local browser happens to have subscribed.
 */
public class GetNotificationsEnabledServiceCase {

    private final GetUserNotificationsEnabledPersistencePort getUserNotificationsEnabledPersistencePort;

    public GetNotificationsEnabledServiceCase(
            GetUserNotificationsEnabledPersistencePort getUserNotificationsEnabledPersistencePort) {
        this.getUserNotificationsEnabledPersistencePort = getUserNotificationsEnabledPersistencePort;
    }

    public boolean getNotificationsEnabled(String userId) {
        return getUserNotificationsEnabledPersistencePort.isNotificationsEnabled(userId);
    }
}
