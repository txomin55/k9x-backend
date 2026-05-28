package com.k9x.infrastructure.out.postgres.collections;

import com.k9x.application.collections.use_case.dto.FetchCollectionScoreDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GetCollectionScoresJooqAdapterTest {

    private static final Field<?>[] SELECT_FIELDS = {
            Tables.EVENT_SCORES.DOG_ID, Tables.EVENT_SCORES.EXERCISE_ID,
            Tables.EVENT_SCORES.JUDGE_ID, Tables.EVENT_SCORES.SCORE
    };

    @Test
    void generates_sql_with_score_fields_filtered_by_event_id() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(SELECT_FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetCollectionScoresJooqAdapter(dsl).getScores("event-1");

        assertThat(capturedSql.get())
                .contains("from \"obdx\".\"event_scores\"")
                .contains("\"obdx\".\"event_scores\".\"event_id\" = ?");
        assertThat(capturedBindings.get()).containsExactly("event-1");
    }

    @Test
    void maps_record_to_dto() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(SELECT_FIELDS);
            Record record = mockDsl.newRecord(SELECT_FIELDS);
            record.set(Tables.EVENT_SCORES.DOG_ID, "dog-1");
            record.set(Tables.EVENT_SCORES.EXERCISE_ID, "exercise-1");
            record.set(Tables.EVENT_SCORES.JUDGE_ID, "judge-1");
            record.set(Tables.EVENT_SCORES.SCORE, new BigDecimal("7.5"));
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchCollectionScoreDTO> scores = new GetCollectionScoresJooqAdapter(dsl).getScores("event-1");

        assertThat(scores).hasSize(1);
        FetchCollectionScoreDTO score = scores.getFirst();
        assertThat(score.dogId()).isEqualTo("dog-1");
        assertThat(score.exerciseId()).isEqualTo("exercise-1");
        assertThat(score.judgeId()).isEqualTo("judge-1");
        assertThat(score.score()).isEqualByComparingTo(new BigDecimal("7.5"));
    }

    @Test
    void returns_empty_list_when_no_results() {
        MockDataProvider provider = _ -> new MockResult[]{
                new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult(SELECT_FIELDS))
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchCollectionScoreDTO> scores = new GetCollectionScoresJooqAdapter(dsl).getScores("event-1");

        assertThat(scores).isEmpty();
    }
}
