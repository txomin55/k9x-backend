package com.k9x.infrastructure.out.postgres.users;

import com.k9x.application.users.port.GetUserNotificationsEnabledPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class GetUserNotificationsEnabledJooqAdapter implements GetUserNotificationsEnabledPersistencePort {

    private final DSLContext dsl;

    public GetUserNotificationsEnabledJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public boolean isNotificationsEnabled(String userId) {
        return dsl.select(Tables.USERS.NOTIFICATIONS_ENABLED)
                .from(Tables.USERS)
                .where(Tables.USERS.ID.eq(userId))
                .fetchOptional(r -> r.get(Tables.USERS.NOTIFICATIONS_ENABLED))
                // Un usuario que no existe no recibe nada; el caso solo se da si la cuenta se borro
                // entre la autenticacion y esta lectura.
                .orElse(false);
    }
}
