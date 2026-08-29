package com.k9x.infrastructure.out.postgres.judges;

import com.k9x.domain.judges.aggregates.Judge;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GetJudgeListJooqAdapterTest {

    @Test
    void generates_sql_filtered_by_creator_and_not_deleted() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.JUDGES.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetJudgeListJooqAdapter(dsl).getJudges("creator-123", null);

        assertThat(capturedSql.get())
                .contains("""
                        select "k9x"."judges"."id", "k9x"."judges"."name", \
                        "k9x"."judges"."creator", "k9x"."judges"."last_update", \
                        "k9x"."judges"."created_at", "k9x"."judges"."deleted_at"\
                        """)
                .contains("from \"k9x\".\"judges\"")
                .contains("where (\"k9x\".\"judges\".\"creator\" = ? and \"k9x\".\"judges\".\"deleted_at\" is null)");
        assertThat(capturedBindings.get()).containsExactly("creator-123");
    }

    @Test
    void generates_sql_filtered_by_country_when_one_is_given() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.JUDGES.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetJudgeListJooqAdapter(dsl).getJudges(null, "ES");

        assertThat(capturedSql.get()).contains("\"k9x\".\"judges\".\"country\" = ?");
        assertThat(capturedBindings.get()).containsExactly("ES");
    }

    @Test
    void generates_sql_filtered_only_by_not_deleted_when_creator_is_null() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.JUDGES.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetJudgeListJooqAdapter(dsl).getJudges(null, null);

        assertThat(capturedSql.get())
                .contains("from \"k9x\".\"judges\"")
                .contains("where \"k9x\".\"judges\".\"deleted_at\" is null")
                .doesNotContain("\"creator\" = ?");
        assertThat(capturedBindings.get()).isEmpty();
    }

    @Test
    void maps_records_to_judge_domain() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(Tables.JUDGES.fields());
            Record record = mockDsl.newRecord(Tables.JUDGES.fields());
            record.set(Tables.JUDGES.ID, "id-1");
            record.set(Tables.JUDGES.NAME, "Rex");
            record.set(Tables.JUDGES.CREATOR, "creator-123");
            record.set(Tables.JUDGES.COUNTRY, "ES");
            record.set(Tables.JUDGES.LAST_UPDATE, 1000L);
            record.set(Tables.JUDGES.CREATED_AT, 2000L);
            record.set(Tables.JUDGES.DELETED_AT, null);
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<Judge> judges = new GetJudgeListJooqAdapter(dsl).getJudges("creator-123", null);

        assertThat(judges).hasSize(1);
        Judge judge = judges.getFirst();
        assertThat(judge.id()).isEqualTo("id-1");
        assertThat(judge.name()).isEqualTo("Rex");
        assertThat(judge.creator()).isEqualTo("creator-123");
        assertThat(judge.country()).isEqualTo("ES");
        assertThat(judge.lastUpdate()).isEqualTo(1000L);
        assertThat(judge.createdAt()).isEqualTo(2000L);
        assertThat(judge.deletedAt()).isNull();
    }
}
