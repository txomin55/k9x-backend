package com.k9x.infrastructure.out.postgres.events.obdx;

import com.k9x.application.events.obdx.use_case.dto.FetchClassificationRawRowDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventCompetitors;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventExercises;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventJudges;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GetClassificationJooqAdapterTest {

    private static final EventCompetitors EC = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS;
    private static final EventExercises EE = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_EXERCISES;
    private static final EventJudges EJ = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_JUDGES;
    private static final Field<?>[] FIELDS = {
            EC.DOG_ID,
            DSL.field("dog_name", String.class),
            Tables.DOGS.OWNER,
            Tables.DOGS.TEAM,
            Tables.DOGS.COUNTRY,
            EE.EXERCISE_ID,
            DSL.field("exercise_position", Short.class),
            EE.TAGS,
            EJ.JUDGE_ID,
            DSL.field("judge_name", String.class),
            DSL.field("score", BigDecimal.class),
            DSL.field("score_last_update", Long.class)
    };

    @Test
    void generates_sql_with_correct_joins_where_and_order() {
        AtomicReference<String> capturedSql = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetClassificationJooqAdapter(dsl).getClassification("evt-1");

        assertThat(capturedSql.get())
                .contains("from \"obdx\".\"event_competitors\"")
                .contains("join \"k9x\".\"dogs\"")
                .contains("join \"obdx\".\"event_exercises\"")
                .contains("join \"obdx\".\"event_judges\"")
                .contains("left outer join \"obdx\".\"event_scores\"")
                .contains("\"obdx\".\"event_competitors\".\"event_id\" = ?")
                .contains("order by \"obdx\".\"event_exercises\".\"position\"");
    }

    @Test
    void returns_empty_list_when_no_competitors() {
        MockDataProvider provider = _ -> {
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchClassificationRawRowDTO> result = new GetClassificationJooqAdapter(dsl).getClassification("evt-1");

        assertThat(result).isEmpty();
    }

    @Test
    void maps_row_to_fetch_classification_raw_row_dto() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(FIELDS);
            Record r = mockDsl.newRecord(FIELDS);
            r.set(EC.DOG_ID, "dog-1");
            r.set(DSL.field("dog_name", String.class), "Rex");
            r.set(Tables.DOGS.OWNER, "owner@test.com");
            r.set(Tables.DOGS.TEAM, "Team A");
            r.set(Tables.DOGS.COUNTRY, "ES");
            r.set(EE.EXERCISE_ID, "ex-1");
            r.set(DSL.field("exercise_position", Short.class), (short) 1);
            r.set(EE.TAGS, new String[]{});
            r.set(EJ.JUDGE_ID, "j-1");
            r.set(DSL.field("judge_name", String.class), "Judge 1");
            r.set(DSL.field("score", BigDecimal.class), new BigDecimal("7.5"));
            r.set(DSL.field("score_last_update", Long.class), 5000L);
            result.add(r);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchClassificationRawRowDTO> rows = new GetClassificationJooqAdapter(dsl).getClassification("evt-1");

        assertThat(rows).hasSize(1);
        FetchClassificationRawRowDTO row = rows.getFirst();
        assertThat(row.dogId()).isEqualTo("dog-1");
        assertThat(row.dogName()).isEqualTo("Rex");
        assertThat(row.dogOwner()).isEqualTo("owner@test.com");
        assertThat(row.exerciseId()).isEqualTo("ex-1");
        assertThat(row.exercisePosition()).isEqualTo((short) 1);
        assertThat(row.exerciseTags()).isEmpty();
        assertThat(row.judgeId()).isEqualTo("j-1");
        assertThat(row.judgeName()).isEqualTo("Judge 1");
        assertThat(row.score()).isEqualByComparingTo("7.5");
        assertThat(row.scoreLastUpdate()).isEqualTo(5000L);
    }

    @Test
    void maps_null_score_when_left_join_has_no_match() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(FIELDS);
            Record r = mockDsl.newRecord(FIELDS);
            r.set(EC.DOG_ID, "dog-1");
            r.set(DSL.field("dog_name", String.class), "Rex");
            r.set(Tables.DOGS.OWNER, "owner@test.com");
            r.set(Tables.DOGS.TEAM, "Team A");
            r.set(Tables.DOGS.COUNTRY, "ES");
            r.set(EE.EXERCISE_ID, "ex-1");
            r.set(DSL.field("exercise_position", Short.class), (short) 1);
            r.set(EE.TAGS, new String[]{});
            r.set(EJ.JUDGE_ID, "j-1");
            r.set(DSL.field("judge_name", String.class), "Judge 1");
            r.set(DSL.field("score", BigDecimal.class), null);
            r.set(DSL.field("score_last_update", Long.class), null);
            result.add(r);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchClassificationRawRowDTO> rows = new GetClassificationJooqAdapter(dsl).getClassification("evt-1");

        assertThat(rows.getFirst().score()).isNull();
        assertThat(rows.getFirst().scoreLastUpdate()).isNull();
    }
}
