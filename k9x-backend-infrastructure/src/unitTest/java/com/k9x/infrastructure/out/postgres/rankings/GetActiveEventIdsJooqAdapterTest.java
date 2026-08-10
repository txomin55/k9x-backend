package com.k9x.infrastructure.out.postgres.rankings;

import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GetActiveEventIdsJooqAdapterTest {

    private static final Field<?>[] FIELDS = {Tables.EVENTS.ID};

    @Test
    void filters_by_the_requested_ids_and_skips_deleted_events() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetActiveEventIdsJooqAdapter(dsl).getActiveEventIds(List.of("event-1", "event-2"));

        assertThat(capturedSql.get())
                .contains("from \"k9x\".\"events\"")
                .contains("\"deleted_at\" is null");
        assertThat(capturedBindings.get()).contains("event-1", "event-2");
    }

    @Test
    void returns_only_the_ids_that_came_back() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(FIELDS);
            Record record = mockDsl.newRecord(FIELDS);
            record.set(Tables.EVENTS.ID, "event-1");
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        Set<String> active = new GetActiveEventIdsJooqAdapter(dsl)
                .getActiveEventIds(List.of("event-1", "event-2"));

        assertThat(active).containsExactly("event-1");
    }

    @Test
    void does_not_hit_the_database_when_no_ids_are_requested() {
        AtomicInteger queries = new AtomicInteger();
        MockDataProvider provider = _ -> {
            queries.incrementAndGet();
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);

        assertThat(new GetActiveEventIdsJooqAdapter(dsl).getActiveEventIds(List.of())).isEmpty();
        assertThat(queries.get()).isZero();
    }
}
