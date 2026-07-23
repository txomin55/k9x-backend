package com.k9x.application.users.use_case;

import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.application.users.port.RegisterPushSubscriptionPersistencePort;
import com.k9x.application.users.port.payload.RegisterPushSubscriptionPersistencePayload;
import com.k9x.application.users.use_case.command.RegisterPushSubscriptionCommand;

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
