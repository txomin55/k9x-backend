package com.k9x.infrastructure.out.postgres.subscriptions;

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

class CreateUserSubscriptionsJooqAdapterTest {

    @Test
    void generates_insert_with_empty_event_ids_and_does_nothing_on_conflict() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult();
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new CreateUserSubscriptionsJooqAdapter(dsl).createUserSubscriptions("user@example.com");

        assertThat(capturedSql.get())
                .contains("insert into \"k9x\".\"user_subscriptions\"")
                .contains("\"user_id\"")
                .contains("\"event_ids\"")
                .contains("on conflict (\"user_id\") do nothing");
        assertThat(capturedBindings.get()[0]).isEqualTo("user@example.com");
    }
}
