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

class GetPushSubscriptionsJooqAdapterTest {

    private final AtomicReference<String> capturedSql = new AtomicReference<>();

    private final MockDataProvider provider = ctx -> {
        capturedSql.set(ctx.sql());
        Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult();
        return new MockResult[]{new MockResult(0, result)};
    };

    private final DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);

    @Test
    void skips_the_subscriptions_of_a_user_that_turned_notifications_off() {
        new GetPushSubscriptionsJooqAdapter(dsl).getByUserId("user@example.com");

        assertThat(capturedSql.get())
                .contains("from \"k9x\".\"push_subscriptions\"")
                .contains("\"user_id\" = ?")
                .contains("\"k9x\".\"users\"")
                .contains("\"notifications_enabled\" = ");
    }
}
