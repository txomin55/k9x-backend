package com.k9x.application.users.port.payload;

import com.k9x.application.users.use_case.command.RegisterPushSubscriptionCommand;
import com.k9x.application.utils.date.DateUtils;

public record RegisterPushSubscriptionPersistencePayload(String endpoint, String userId, String auth,
        String p256dh, long lastUpdate) {

    public static RegisterPushSubscriptionPersistencePayload from(RegisterPushSubscriptionCommand command, String userId) {
        return new RegisterPushSubscriptionPersistencePayload(
                command.endpoint(), userId, command.auth(), command.p256dh(), DateUtils.nowUtcMillis());
    }
}
