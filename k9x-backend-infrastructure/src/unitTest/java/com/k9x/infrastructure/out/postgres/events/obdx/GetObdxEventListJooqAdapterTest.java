package com.k9x.infrastructure.out.postgres.events.obdx;

import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Stages;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.Events;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GetObdxEventListJooqAdapterTest {

    @Test
    void generates_sql_filtered_by_stage_ids_and_not_deleted() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult();
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetObdxEventListJooqAdapter(dsl).getEvents(List.of("stage-1", "stage-2"));

        assertThat(capturedSql.get())
                .contains("from \"obdx\".\"events\"")
                .contains("join \"k9x\".\"stages\"")
                .contains("\"obdx\".\"events\".\"stage_id\" in (?, ?)")
                .contains("\"obdx\".\"events\".\"deleted_at\" is null");
        assertThat(capturedBindings.get()).containsExactly("stage-1", "stage-2");
    }

    @Test
    void maps_record_to_fetch_obdx_event_dto() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Events e = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENTS;
            Stages s = com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables.STAGES;

            var eventId = e.ID.as("event_id");
            var eventName = e.NAME.as("event_name");
            var stageId = s.ID.as("stage_id");
            var stageName = s.NAME.as("stage_name");

            Field<?>[] fields = {eventId, eventName, stageId, stageName};
            Result<Record> result = mockDsl.newResult(fields);
            Record record = mockDsl.newRecord(fields);
            record.set(eventId, "event-1");
            record.set(eventName, "Event 1");
            record.set(stageId, "stage-1");
            record.set(stageName, "Stage 1");
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchObdxEventDTO> events = new GetObdxEventListJooqAdapter(dsl).getEvents(List.of("stage-1"));

        assertThat(events).hasSize(1);
        assertThat(events.getFirst().id()).isEqualTo("event-1");
        assertThat(events.getFirst().name()).isEqualTo("Event 1");
        assertThat(events.getFirst().stageId()).isEqualTo("stage-1");
        assertThat(events.getFirst().stageName()).isEqualTo("Stage 1");
    }

    @Test
    void returns_empty_list_when_no_events_found() {
        MockDataProvider provider = _ -> {
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult();
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchObdxEventDTO> events = new GetObdxEventListJooqAdapter(dsl).getEvents(List.of("stage-1"));

        assertThat(events).isEmpty();
    }
}
