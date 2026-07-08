package com.k9x.infrastructure.out.postgres.collections;

import com.k9x.application.collections.use_case.dto.FetchCollectionExerciseDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables;
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

class GetObdxCollectionExercisesJooqAdapterTest {

    private static final Field<?>[] SELECT_FIELDS = {
            Tables.EVENT_EXERCISES.EXERCISE_ID, Tables.EVENT_EXERCISES.POSITION, Tables.EVENT_EXERCISES.JUDGES
    };

    @Test
    void generates_sql_with_exercise_id_and_position_filtered_by_event_id() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(SELECT_FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetObdxCollectionExercisesJooqAdapter(dsl).getExercises("event-1");

        assertThat(capturedSql.get())
                .contains("from \"obdx\".\"event_exercises\"")
                .contains("\"obdx\".\"event_exercises\".\"event_id\" = ?");
        assertThat(capturedBindings.get()).containsExactly("event-1");
    }

    @Test
    void maps_record_to_dto() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(SELECT_FIELDS);
            Record record = mockDsl.newRecord(SELECT_FIELDS);
            record.set(Tables.EVENT_EXERCISES.EXERCISE_ID, "exercise-1");
            record.set(Tables.EVENT_EXERCISES.POSITION, (short) 1);
            record.set(Tables.EVENT_EXERCISES.JUDGES, new String[]{"judge-1", "judge-2"});
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchCollectionExerciseDTO> exercises = new GetObdxCollectionExercisesJooqAdapter(dsl).getExercises("event-1");

        assertThat(exercises).hasSize(1);
        assertThat(exercises.getFirst().exerciseId()).isEqualTo("exercise-1");
        assertThat(exercises.getFirst().position()).isEqualTo((short) 1);
        assertThat(exercises.getFirst().judges()).containsExactly("judge-1", "judge-2");
    }

    @Test
    void returns_empty_list_when_no_results() {
        MockDataProvider provider = _ -> new MockResult[]{
                new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult(SELECT_FIELDS))
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchCollectionExerciseDTO> exercises = new GetObdxCollectionExercisesJooqAdapter(dsl).getExercises("event-1");

        assertThat(exercises).isEmpty();
    }
}
