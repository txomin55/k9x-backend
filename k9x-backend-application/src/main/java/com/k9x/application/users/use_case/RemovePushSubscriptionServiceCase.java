package com.k9x.application.users.use_case;

import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.application.users.port.DeletePushSubscriptionPersistencePort;
import com.k9x.application.users.use_case.command.RemovePushSubscriptionCommand;

/**
 * Unsubscribes a single device: the endpoint identifies the browser installation that asked to stop
 * receiving pushes, so the user's other devices keep theirs. Deleting an endpoint that does not exist
 * — or belongs to someone else — is a no-op rather than an error, so a client can always retry.
 */
public class RemovePushSubscriptionServiceCase implements TransactionalUseCase {

    private final DeletePushSubscriptionPersistencePort deletePushSubscriptionPersistencePort;

    public RemovePushSubscriptionServiceCase(DeletePushSubscriptionPersistencePort deletePushSubscriptionPersistencePort) {
        this.deletePushSubscriptionPersistencePort = deletePushSubscriptionPersistencePort;
    }

    public void removePushSubscription(RemovePushSubscriptionCommand command, String userId) {
        deletePushSubscriptionPersistencePort.deleteByEndpointAndUserId(command.endpoint(), userId);
    }
}
