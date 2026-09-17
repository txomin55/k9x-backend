package com.k9x.application.users.use_case;

import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.application.users.port.RegisterPushSubscriptionPersistencePort;
import com.k9x.application.users.port.payload.RegisterPushSubscriptionPersistencePayload;
import com.k9x.application.users.use_case.command.RegisterPushSubscriptionCommand;

/**
 * Records the calling device as a delivery target. Nothing more: whether the user actually receives
 * notifications is an account setting owned by {@link SetNotificationsEnabledServiceCase}.
 *
 * <p>The two are kept apart because the client re-registers on every start, long after the permission
 * was granted, so a registration carries no intent — treating it as one would silently un-mute an
 * account the user had turned off from another device. A device registered while the account is off is
 * simply not a target until the user turns notifications back on, and then it is one without having to
 * be visited.
 *
 * <p>Registering is idempotent: a known endpoint keeps its stored keys untouched.
 */
public class RegisterPushSubscriptionServiceCase implements TransactionalUseCase {

    private final RegisterPushSubscriptionPersistencePort registerPushSubscriptionPersistencePort;

    public RegisterPushSubscriptionServiceCase(RegisterPushSubscriptionPersistencePort registerPushSubscriptionPersistencePort) {
        this.registerPushSubscriptionPersistencePort = registerPushSubscriptionPersistencePort;
    }

    public void registerPushSubscription(RegisterPushSubscriptionCommand command, String userId) {
        registerPushSubscriptionPersistencePort.registerPushSubscription(
                RegisterPushSubscriptionPersistencePayload.from(command, userId));
    }
}
