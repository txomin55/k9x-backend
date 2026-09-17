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

class SetUserNotificationsEnabledJooqAdapterTest {

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
    void stores_the_preference_on_the_account_and_not_on_a_device() {
        new SetUserNotificationsEnabledJooqAdapter(dsl).setNotificationsEnabled("user@example.com", false);

        assertThat(capturedSql.get())
                .contains("update \"k9x\".\"users\"")
                .contains("\"notifications_enabled\"")
                .doesNotContain("push_subscriptions");
        assertThat(capturedBindings.get()).containsExactly(false, "user@example.com");
    }
}
