package com.k9x.infrastructure.out.postgres.users;

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

class DeletePushSubscriptionJooqAdapterTest {

    private final AtomicReference<String> capturedSql = new AtomicReference<>();
    private final AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

    private final MockDataProvider provider = ctx -> {
        capturedSql.set(ctx.sql());
        capturedBindings.set(ctx.bindings());
        Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult();
        return new MockResult[]{new MockResult(1, result)};
    };

    private final DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);

    @Test
    void deletes_by_endpoint_only_when_pruning_an_expired_subscription() {
        new DeletePushSubscriptionJooqAdapter(dsl).deleteByEndpoint("https://fcm/endpoint");

        assertThat(capturedSql.get())
                .contains("delete from \"k9x\".\"push_subscriptions\"")
                .contains("\"endpoint\"")
                .doesNotContain("\"user_id\"");
        assertThat(capturedBindings.get()).containsExactly("https://fcm/endpoint");
    }

    @Test
    void scopes_deletion_to_the_owner_when_the_user_unsubscribes() {
        new DeletePushSubscriptionJooqAdapter(dsl).deleteByEndpointAndUserId("https://fcm/endpoint", "user@example.com");

        assertThat(capturedSql.get())
                .contains("delete from \"k9x\".\"push_subscriptions\"")
                .contains("\"endpoint\"")
                .contains("\"user_id\"");
        assertThat(capturedBindings.get()).containsExactly("https://fcm/endpoint", "user@example.com");
    }
}
