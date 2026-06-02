package com.k9x.infrastructure.out.postgres.events;

import com.k9x.infrastructure.out.postgres.events.CreateEventJooqAdapter;
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

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CreateEventJooqAdapterTest {

    @Test
    void generates_insert_sql_with_all_fields() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.EVENTS.fields());
            return new MockResult[]{new MockResult(1, result)};
        };

        long createdAt = 1700000000000L;
        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new CreateEventJooqAdapter(dsl).createEvent("event-1", "Event 1", "stage-1", "obdx", "user-1", createdAt);

        assertThat(capturedSql.get())
                .contains("insert into \"k9x\".\"events\"")
                .contains("\"id\"")
                .contains("\"name\"")
                .contains("\"stage_id\"")
                .contains("\"discipline\"")
                .contains("\"creator\"")
                .contains("\"created_at\"")
                .contains("\"last_update\"");
        assertThat(capturedBindings.get()).contains("event-1", "Event 1", "stage-1", "obdx", "user-1", createdAt);
    }
}
