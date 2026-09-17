package com.k9x.application.users.use_case;

import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.application.users.port.SetUserNotificationsEnabledPersistencePort;

/**
 * Switches the account's notifications on or off for every device at once.
 *
 * <p>Turning them off keeps the subscriptions: a deleted one could never be restored from the server —
 * its keys belong to the browser that created them — so dropping them would make turning notifications
 * back on reach only the device that asked. Nothing is pushed while the account is off because
 * {@code GetPushSubscriptionsPersistencePort} resolves no targets for it.
 *
 * <p>Setting the value it already has is a no-op rather than an error, so a client can always retry.
 */
public class SetNotificationsEnabledServiceCase implements TransactionalUseCase {

    private final SetUserNotificationsEnabledPersistencePort setUserNotificationsEnabledPersistencePort;

    public SetNotificationsEnabledServiceCase(
            SetUserNotificationsEnabledPersistencePort setUserNotificationsEnabledPersistencePort) {
        this.setUserNotificationsEnabledPersistencePort = setUserNotificationsEnabledPersistencePort;
    }

    public void setNotificationsEnabled(String userId, boolean enabled) {
        setUserNotificationsEnabledPersistencePort.setNotificationsEnabled(userId, enabled);
    }
}
