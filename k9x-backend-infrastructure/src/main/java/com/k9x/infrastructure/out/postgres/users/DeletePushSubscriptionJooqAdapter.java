package com.k9x.infrastructure.out.postgres.users;

import com.k9x.application.users.port.DeletePushSubscriptionPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class DeletePushSubscriptionJooqAdapter implements DeletePushSubscriptionPersistencePort {

    private final DSLContext dsl;

    public DeletePushSubscriptionJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void deleteByEndpoint(String endpoint) {
        dsl.deleteFrom(Tables.PUSH_SUBSCRIPTIONS)
                .where(Tables.PUSH_SUBSCRIPTIONS.ENDPOINT.eq(endpoint))
                .execute();
    }

    @Override
    public void deleteByEndpointAndUserId(String endpoint, String userId) {
        dsl.deleteFrom(Tables.PUSH_SUBSCRIPTIONS)
                .where(Tables.PUSH_SUBSCRIPTIONS.ENDPOINT.eq(endpoint))
                .and(Tables.PUSH_SUBSCRIPTIONS.USER_ID.eq(userId))
                .execute();
    }
}
