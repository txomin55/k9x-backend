package com.k9x.infrastructure.out.postgres.events;

import com.k9x.application.events.obdx.port.payload.EnrollObdxEventPersistencePayload;
import com.k9x.infrastructure.out.postgres.events.EnrollEventJooqAdapter;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables;
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

class EnrollEventJooqAdapterTest {

    @Test
    void generates_insert_sql_with_verified_false_and_no_position() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.EVENT_COMPETITORS.fields());
            return new MockResult[]{new MockResult(1, result)};
        };

        long lastUpdate = 1700000000000L;
        EnrollObdxEventPersistencePayload payload = new EnrollObdxEventPersistencePayload("dog-1", lastUpdate);
        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new EnrollEventJooqAdapter(dsl).enrollEvent("event-1", payload);

        assertThat(capturedSql.get())
                .contains("insert into \"obdx\".\"event_competitors\"")
                .contains("\"event_id\"")
                .contains("\"dog_id\"")
                .contains("\"verified\"")
                .contains("\"last_update\"")
                .doesNotContain("\"position\"");
        assertThat(capturedBindings.get()).contains("event-1", "dog-1", false, lastUpdate);
    }
}
