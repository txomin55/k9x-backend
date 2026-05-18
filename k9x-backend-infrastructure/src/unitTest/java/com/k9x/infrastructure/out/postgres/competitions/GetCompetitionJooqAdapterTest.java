package com.k9x.infrastructure.out.postgres.competitions;

import com.k9x.domain.aggregates.competitions.Competition;
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

class GetCompetitionJooqAdapterTest {

    @Test
    void generates_sql_filtered_by_id() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.COMPETITIONS.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetCompetitionJooqAdapter(dsl).getCompetition("comp-123");

        assertThat(capturedSql.get())
                .contains("from \"k9x\".\"competitions\"")
                .contains("where \"k9x\".\"competitions\".\"id\" = ?");
        assertThat(capturedBindings.get()).containsExactly("comp-123");
    }

    @Test
    void maps_record_to_competition_domain() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(Tables.COMPETITIONS.fields());
            Record record = mockDsl.newRecord(Tables.COMPETITIONS.fields());
            record.set(Tables.COMPETITIONS.ID, "comp-123");
            record.set(Tables.COMPETITIONS.NAME, "World Cup");
            record.set(Tables.COMPETITIONS.CREATOR, "user-1");
            record.set(Tables.COMPETITIONS.LAST_UPDATE, 1000L);
            record.set(Tables.COMPETITIONS.CREATED_AT, 2000L);
            record.set(Tables.COMPETITIONS.DELETED_AT, null);
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        Competition competition = new GetCompetitionJooqAdapter(dsl).getCompetition("comp-123");

        assertThat(competition.id()).isEqualTo("comp-123");
        assertThat(competition.name()).isEqualTo("World Cup");
        assertThat(competition.creator()).isEqualTo("user-1");
        assertThat(competition.lastUpdate()).isEqualTo(1000L);
        assertThat(competition.createdAt()).isEqualTo(2000L);
        assertThat(competition.deletedAt()).isNull();
    }

    @Test
    void returns_null_when_competition_not_found() {
        MockDataProvider provider = _ -> {
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.COMPETITIONS.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        Competition competition = new GetCompetitionJooqAdapter(dsl).getCompetition("comp-123");

        assertThat(competition).isNull();
    }
}
