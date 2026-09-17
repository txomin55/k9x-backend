package com.k9x.infrastructure.out.postgres.users;

import com.k9x.application.users.port.SetUserNotificationsEnabledPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class SetUserNotificationsEnabledJooqAdapter implements SetUserNotificationsEnabledPersistencePort {

    private final DSLContext dsl;

    public SetUserNotificationsEnabledJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void setNotificationsEnabled(String userId, boolean enabled) {
        dsl.update(Tables.USERS)
                .set(Tables.USERS.NOTIFICATIONS_ENABLED, enabled)
                .where(Tables.USERS.ID.eq(userId))
                .execute();
    }
}
