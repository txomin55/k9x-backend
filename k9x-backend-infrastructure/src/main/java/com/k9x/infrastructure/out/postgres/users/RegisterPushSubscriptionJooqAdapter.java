package com.k9x.infrastructure.out.postgres.users;

import com.k9x.application.users.port.RegisterPushSubscriptionPersistencePort;
import com.k9x.application.users.port.payload.RegisterPushSubscriptionPersistencePayload;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class RegisterPushSubscriptionJooqAdapter implements RegisterPushSubscriptionPersistencePort {

    private final DSLContext dsl;

    public RegisterPushSubscriptionJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void registerPushSubscription(RegisterPushSubscriptionPersistencePayload payload) {
        dsl.insertInto(Tables.PUSH_SUBSCRIPTIONS)
                .set(Tables.PUSH_SUBSCRIPTIONS.ENDPOINT, payload.endpoint())
                .set(Tables.PUSH_SUBSCRIPTIONS.USER_ID, payload.userId())
                .set(Tables.PUSH_SUBSCRIPTIONS.AUTH, payload.auth())
                .set(Tables.PUSH_SUBSCRIPTIONS.P256DH, payload.p256dh())
                .set(Tables.PUSH_SUBSCRIPTIONS.CREATED_AT, payload.lastUpdate())
                .set(Tables.PUSH_SUBSCRIPTIONS.LAST_UPDATE, payload.lastUpdate())
                .onConflict(Tables.PUSH_SUBSCRIPTIONS.ENDPOINT)
                .doUpdate()
                .set(Tables.PUSH_SUBSCRIPTIONS.USER_ID, payload.userId())
                .set(Tables.PUSH_SUBSCRIPTIONS.AUTH, payload.auth())
                .set(Tables.PUSH_SUBSCRIPTIONS.P256DH, payload.p256dh())
                .set(Tables.PUSH_SUBSCRIPTIONS.LAST_UPDATE, payload.lastUpdate())
                .execute();
    }
}
