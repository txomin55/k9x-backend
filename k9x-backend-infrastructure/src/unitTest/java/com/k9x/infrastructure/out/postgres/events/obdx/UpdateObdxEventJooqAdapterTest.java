package com.k9x.infrastructure.out.postgres.events.obdx;

import com.k9x.application.events.obdx.port.payload.UpdateObdxEventPersistencePayload;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateObdxEventJooqAdapterTest {

    @Test
    void generates_update_event_and_replaces_competitors_exercises_and_judges() {
        List<String> capturedSqls = new ArrayList<>();
        List<Object[]> capturedBindings = new ArrayList<>();

        MockDataProvider provider = ctx -> {
            capturedSqls.add(ctx.sql());
            capturedBindings.add(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult();
            return new MockResult[]{new MockResult(1, result)};
        };

        long lastUpdate = 1700000000000L;
        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);

        UpdateObdxEventPersistencePayload payload = new UpdateObdxEventPersistencePayload(
                "Event 1",
                "config-1",
                List.of(new UpdateObdxEventPersistencePayload.CompetitorItem("dog-1", (short) 1)),
                List.of(new UpdateObdxEventPersistencePayload.ExerciseItem("exercise-1", (short) 1, new String[]{"tag1"})),
                List.of(new UpdateObdxEventPersistencePayload.JudgeItem("judge-1", "collector@example.com")),
                lastUpdate
        );

        new UpdateObdxEventJooqAdapter(dsl).updateEvent("event-1", payload);

        assertThat(capturedSqls).hasSize(7);

        assertThat(capturedSqls.get(0))
                .contains("update \"obdx\".\"events\"")
                .contains("\"name\"")
                .contains("\"configuration_id\"")
                .contains("\"last_update\"")
                .contains("\"id\"");
        assertThat(capturedBindings.get(0)).contains("event-1", "Event 1", "config-1", lastUpdate);

        assertThat(capturedSqls.get(1))
                .contains("delete from \"obdx\".\"event_competitors\"")
                .contains("\"event_id\"");
        assertThat(capturedBindings.get(1)).contains("event-1");

        assertThat(capturedSqls.get(2))
                .contains("insert into \"obdx\".\"event_competitors\"")
                .contains("\"event_id\"")
                .contains("\"dog_id\"")
                .contains("\"position\"")
                .contains("\"verified\"")
                .contains("\"last_update\"");
        assertThat(capturedBindings.get(2)).contains("event-1", "dog-1", (short) 1, true, lastUpdate);

        assertThat(capturedSqls.get(3))
                .contains("delete from \"obdx\".\"event_exercises\"")
                .contains("\"event_id\"");
        assertThat(capturedBindings.get(3)).contains("event-1");

        assertThat(capturedSqls.get(4))
                .contains("insert into \"obdx\".\"event_exercises\"")
                .contains("\"event_id\"")
                .contains("\"exercise_id\"")
                .contains("\"position\"")
                .contains("\"tags\"")
                .contains("\"last_update\"");
        assertThat(capturedBindings.get(4)).contains("event-1", "exercise-1", (short) 1, lastUpdate);

        assertThat(capturedSqls.get(5))
                .contains("delete from \"obdx\".\"event_judges\"")
                .contains("\"event_id\"");
        assertThat(capturedBindings.get(5)).contains("event-1");

        assertThat(capturedSqls.get(6))
                .contains("insert into \"obdx\".\"event_judges\"")
                .contains("\"event_id\"")
                .contains("\"judge_id\"")
                .contains("\"collector_id\"")
                .contains("\"last_update\"");
        assertThat(capturedBindings.get(6)).contains("event-1", "judge-1", "collector@example.com", lastUpdate);
    }

    @Test
    void skips_inserts_when_all_lists_are_empty() {
        List<String> capturedSqls = new ArrayList<>();

        MockDataProvider provider = ctx -> {
            capturedSqls.add(ctx.sql());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult();
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);

        UpdateObdxEventPersistencePayload payload = new UpdateObdxEventPersistencePayload(
                "Event 1", "config-1", List.of(), List.of(), List.of(), 1700000000000L
        );

        new UpdateObdxEventJooqAdapter(dsl).updateEvent("event-1", payload);

        assertThat(capturedSqls).hasSize(4);
        assertThat(capturedSqls.get(0)).contains("update \"obdx\".\"events\"");
        assertThat(capturedSqls.get(1)).contains("delete from \"obdx\".\"event_competitors\"");
        assertThat(capturedSqls.get(2)).contains("delete from \"obdx\".\"event_exercises\"");
        assertThat(capturedSqls.get(3)).contains("delete from \"obdx\".\"event_judges\"");
    }
}
