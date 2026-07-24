package com.k9x.infrastructure.out.postgres.notifications;

import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MarkNotificationsSeenJooqAdapterTest {

    @Test
    void generates_update_scoped_by_user_and_numeric_ids() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.NOTIFICATIONS.fields());
            return new MockResult[]{new MockResult(2, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new MarkNotificationsSeenJooqAdapter(dsl).markSeen("creator-1", List.of("1", "2"));

        assertThat(capturedSql.get())
                .contains("update \"k9x\".\"notifications\"")
                .contains("set \"seen\" = ?")
                .contains("where (\"k9x\".\"notifications\".\"user_id\" = ?")
                .contains("\"k9x\".\"notifications\".\"id\" in (?, ?)");
        assertThat(capturedBindings.get()).containsExactly(true, "creator-1", 1L, 2L);
    }

    @Test
    void ignores_non_numeric_ids_and_skips_query_when_none_are_valid() {
        AtomicInteger queries = new AtomicInteger();
        MockDataProvider provider = ctx -> {
            queries.incrementAndGet();
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.NOTIFICATIONS.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new MarkNotificationsSeenJooqAdapter(dsl).markSeen("creator-1", List.of("not-a-number", "abc"));

        assertThat(queries.get()).isZero();
    }
}
