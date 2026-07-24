package com.k9x.application.notifications.use_case;

import com.k9x.application.notifications.port.MarkNotificationsSeenPersistencePort;
import com.k9x.application.notifications.use_case.command.MarkNotificationsSeenCommand;
import com.k9x.application.shared.TransactionalUseCase;

/**
 * Marks a set of the authenticated user's notifications as seen. Scoping by {@code user_id} in the
 * persistence adapter guarantees a user can never flip the {@code seen} flag on another user's rows.
 */
public class MarkNotificationsSeenServiceCase implements TransactionalUseCase {

    private final MarkNotificationsSeenPersistencePort markNotificationsSeenPersistencePort;

    public MarkNotificationsSeenServiceCase(MarkNotificationsSeenPersistencePort markNotificationsSeenPersistencePort) {
        this.markNotificationsSeenPersistencePort = markNotificationsSeenPersistencePort;
    }

    public void markSeen(MarkNotificationsSeenCommand command, String userId) {
        if (command.ids() == null || command.ids().isEmpty()) {
            return;
        }
        markNotificationsSeenPersistencePort.markSeen(userId, command.ids());
    }
}
