package com.k9x.application.subscriptions.port.payload;

import com.k9x.application.subscriptions.use_case.command.UpdateUserSubscriptionCommand;
import com.k9x.domain.subscriptions.SubscriptionKind;

import java.util.List;

public record UpdateUserSubscriptionPersistencePayload(String userId, SubscriptionKind kind,
        List<String> targetIds, boolean subscribe) {

    public static UpdateUserSubscriptionPersistencePayload from(UpdateUserSubscriptionCommand command,
            SubscriptionKind kind, String userId) {
        return new UpdateUserSubscriptionPersistencePayload(userId, kind, List.copyOf(command.ids()),
                command.subscribe());
    }
}
