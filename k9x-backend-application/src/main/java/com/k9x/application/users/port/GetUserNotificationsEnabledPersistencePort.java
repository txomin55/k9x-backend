package com.k9x.application.users.port;

public interface GetUserNotificationsEnabledPersistencePort {

    /**
     * Reads the account's notification setting. Deliberately not part of the cached {@code UserInfoDTO}:
     * the user flips this from any of their devices and expects the others to reflect it, so it is read
     * fresh instead of served from the session cache.
     */
    boolean isNotificationsEnabled(String userId);
}
