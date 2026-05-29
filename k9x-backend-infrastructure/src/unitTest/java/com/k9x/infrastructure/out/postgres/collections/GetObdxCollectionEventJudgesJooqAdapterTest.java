package com.k9x.infrastructure.out.postgres.collections;

import com.k9x.application.collections.use_case.dto.FetchCollectionJudgeWithCollectorDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Judges;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Users;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventJudges;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class GetObdxCollectionEventJudgesJooqAdapterTest {

    private static final EventJudges EJ = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_JUDGES;
    private static final Judges J = Tables.JUDGES;
    private static final Users U = Tables.USERS;

    private static final Field<?>[] SELECT_FIELDS = {EJ.JUDGE_ID, J.NAME, U.EMAIL};

    private static final Field<?>[] JOIN_FIELDS = Stream.of(
            Arrays.stream(EJ.fields()),
            Arrays.stream(J.fields()),
            Arrays.stream(U.fields())
    ).flatMap(s -> s).toArray(Field[]::new);

    @Test
    void generates_sql_with_joins_filtered_by_event_id() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(JOIN_FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetObdxCollectionEventJudgesJooqAdapter(dsl).getJudges("event-1");

        assertThat(capturedSql.get())
                .contains("from \"obdx\".\"event_judges\"")
                .contains("join \"k9x\".\"judges\"")
                .contains("join \"k9x\".\"users\"")
                .contains("\"obdx\".\"event_judges\".\"event_id\" = ?");
        assertThat(capturedBindings.get()).containsExactly("event-1");
    }

    @Test
    void maps_record_to_dto() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(SELECT_FIELDS);
            Record record = mockDsl.newRecord(SELECT_FIELDS);
            record.set(EJ.JUDGE_ID, "judge-1");
            record.set(J.NAME, "Judge One");
            record.set(U.EMAIL, "collector@test.com");
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchCollectionJudgeWithCollectorDTO> judges = new GetObdxCollectionEventJudgesJooqAdapter(dsl).getJudges("event-1");

        assertThat(judges).hasSize(1);
        assertThat(judges.getFirst().judgeId()).isEqualTo("judge-1");
        assertThat(judges.getFirst().judgeName()).isEqualTo("Judge One");
        assertThat(judges.getFirst().collectorEmail()).isEqualTo("collector@test.com");
    }

    @Test
    void returns_empty_list_when_no_results() {
        MockDataProvider provider = _ -> new MockResult[]{
                new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult(JOIN_FIELDS))
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchCollectionJudgeWithCollectorDTO> judges = new GetObdxCollectionEventJudgesJooqAdapter(dsl).getJudges("event-1");

        assertThat(judges).isEmpty();
    }
}
