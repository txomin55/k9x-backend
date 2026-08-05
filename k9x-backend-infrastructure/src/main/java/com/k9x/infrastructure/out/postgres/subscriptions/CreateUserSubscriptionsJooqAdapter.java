package com.k9x.infrastructure.out.postgres.subscriptions;

import com.k9x.application.subscriptions.port.CreateUserSubscriptionsPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class CreateUserSubscriptionsJooqAdapter implements CreateUserSubscriptionsPersistencePort {

    private static final String[] NO_SUBSCRIPTIONS = new String[0];

    private final DSLContext dsl;

    public CreateUserSubscriptionsJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void createUserSubscriptions(String userId) {
        dsl.insertInto(Tables.USER_SUBSCRIPTIONS)
                .set(Tables.USER_SUBSCRIPTIONS.USER_ID, userId)
                .set(Tables.USER_SUBSCRIPTIONS.EVENT_IDS, NO_SUBSCRIPTIONS)
                .onConflict(Tables.USER_SUBSCRIPTIONS.USER_ID)
                .doNothing()
                .execute();
    }
}
