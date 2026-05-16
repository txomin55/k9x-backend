package com.k9x.infrastructure.out.postgres.judges;

import com.k9x.domain.aggregates.judges.Judge;
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

class GetJudgeJooqAdapterTest {

    @Test
    void generates_sql_filtered_by_id() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.JUDGES.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetJudgeJooqAdapter(dsl).getJudge("judge-123");

        assertThat(capturedSql.get())
                .contains("from \"k9x\".\"judges\"")
                .contains("where \"k9x\".\"judges\".\"id\" = ?");
        assertThat(capturedBindings.get()).containsExactly("judge-123");
    }

    @Test
    void maps_record_to_judge_domain() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(Tables.JUDGES.fields());
            Record record = mockDsl.newRecord(Tables.JUDGES.fields());
            record.set(Tables.JUDGES.ID, "judge-123");
            record.set(Tables.JUDGES.NAME, "Rex");
            record.set(Tables.JUDGES.CREATOR, "user-1");
            record.set(Tables.JUDGES.LAST_UPDATE, 1000L);
            record.set(Tables.JUDGES.CREATED_AT, 2000L);
            record.set(Tables.JUDGES.DELETED_AT, null);
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        Judge judge = new GetJudgeJooqAdapter(dsl).getJudge("judge-123");

        assertThat(judge.id()).isEqualTo("judge-123");
        assertThat(judge.name()).isEqualTo("Rex");
        assertThat(judge.creator()).isEqualTo("user-1");
        assertThat(judge.deletedAt()).isNull();
    }

    @Test
    void returns_null_when_judge_not_found() {
        MockDataProvider provider = _ -> {
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.JUDGES.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        Judge judge = new GetJudgeJooqAdapter(dsl).getJudge("judge-123");

        assertThat(judge).isNull();
    }
}
