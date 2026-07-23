package com.k9x.infrastructure.out.postgres.users;

import com.k9x.application.users.port.payload.RegisterPushSubscriptionPersistencePayload;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterPushSubscriptionJooqAdapterTest {

    @Test
    void generates_upsert_on_endpoint_conflict() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult();
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new RegisterPushSubscriptionJooqAdapter(dsl).registerPushSubscription(
                new RegisterPushSubscriptionPersistencePayload("https://fcm/endpoint", "user@example.com", "auth-key", "p256dh-key", 1700000000000L));

        assertThat(capturedSql.get())
                .contains("insert into \"k9x\".\"push_subscriptions\"")
                .contains("on conflict")
                .contains("\"endpoint\"");
        assertThat(capturedBindings.get()).contains("https://fcm/endpoint", "user@example.com", "auth-key", "p256dh-key");
    }
}
