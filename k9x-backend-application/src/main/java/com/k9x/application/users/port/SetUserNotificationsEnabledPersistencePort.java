package com.k9x.application.users.port;

public interface SetUserNotificationsEnabledPersistencePort {

    /**
     * Turns the account's notifications on or off. The setting belongs to the user, not to the device
     * that sends the request: a browser has no way of reaching the user's other installations, so
     * storing it per subscription would leave each device with its own idea of the same preference.
     */
    void setNotificationsEnabled(String userId, boolean enabled);
}
