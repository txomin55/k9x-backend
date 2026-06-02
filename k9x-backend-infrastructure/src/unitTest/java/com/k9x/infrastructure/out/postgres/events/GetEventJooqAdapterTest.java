package com.k9x.infrastructure.out.postgres.events;

import com.k9x.domain.aggregates.events.Event;
import com.k9x.infrastructure.out.postgres.events.GetEventJooqAdapter;
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

class GetEventJooqAdapterTest {

    @Test
    void generates_sql_filtered_by_id() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.EVENTS.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetEventJooqAdapter(dsl).getEvent("event-1");

        assertThat(capturedSql.get())
                .contains("from \"obdx\".\"events\"")
                .contains("where \"obdx\".\"events\".\"id\" = ?");
        assertThat(capturedBindings.get()).containsExactly("event-1");
    }

    @Test
    void maps_record_to_obdx_event_domain() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(Tables.EVENTS.fields());
            Record record = mockDsl.newRecord(Tables.EVENTS.fields());
            record.set(Tables.EVENTS.ID, "event-1");
            record.set(Tables.EVENTS.CONFIGURATION_ID, "config-1");
            record.set(Tables.EVENTS.NAME, "Event 1");
            record.set(Tables.EVENTS.STAGE_ID, "stage-1");
            record.set(Tables.EVENTS.CREATOR, "user-1");
            record.set(Tables.EVENTS.LAST_UPDATE, 1000L);
            record.set(Tables.EVENTS.CREATED_AT, 2000L);
            record.set(Tables.EVENTS.DELETED_AT, null);
            record.set(Tables.EVENTS.SCORE_CALCULATION, "MID_AVG");
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        Event event = new GetEventJooqAdapter(dsl).getEvent("event-1");

        assertThat(event.id()).isEqualTo("event-1");
        assertThat(event.configurationId()).isEqualTo("config-1");
        assertThat(event.name()).isEqualTo("Event 1");
        assertThat(event.stageId()).isEqualTo("stage-1");
        assertThat(event.creator()).isEqualTo("user-1");
        assertThat(event.lastUpdate()).isEqualTo(1000L);
        assertThat(event.createdAt()).isEqualTo(2000L);
        assertThat(event.deletedAt()).isNull();
    }

    @Test
    void returns_null_when_event_not_found() {
        MockDataProvider provider = _ -> {
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.EVENTS.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        Event event = new GetEventJooqAdapter(dsl).getEvent("event-1");

        assertThat(event).isNull();
    }
}
