package com.k9x.infrastructure.out.postgres.rankings;

import com.k9x.application.rankings.port.payload.SaveRankingPersistencePayload;
import com.k9x.domain.rankings.RankingIncludeBy;
import com.k9x.domain.rankings.RankingGroupBy;
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

class SaveRankingJooqAdapterTest {

    private static MockDataProvider capturing(AtomicReference<String> sql, AtomicReference<Object[]> bindings) {
        return ctx -> {
            sql.set(ctx.sql());
            bindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult();
            return new MockResult[]{new MockResult(1, result)};
        };
    }

    private static SaveRankingPersistencePayload payload(RankingIncludeBy includeBy, Integer includedCount) {
        return new SaveRankingPersistencePayload("ranking_comp-1", "Copa", List.of("event-1", "event-2"),
                RankingGroupBy.TEAM, includeBy, includedCount, "user-1", 1700000000000L);
    }

    @Test
    void generates_insert_sql_with_all_fields() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        DSLContext dsl = DSL.using(new MockConnection(capturing(capturedSql, capturedBindings)), SQLDialect.POSTGRES);
        new SaveRankingJooqAdapter(dsl).saveRanking(payload(RankingIncludeBy.LOWEST, 1));

        assertThat(capturedSql.get())
                .contains("insert into \"k9x\".\"rankings\"")
                .contains("\"id\"")
                .contains("\"name\"")
                .contains("\"event_ids\"")
                .contains("\"group_by\"")
                .contains("\"include_by\"")
                .contains("\"included_count\"")
                .contains("\"creator\"")
                .contains("\"created_at\"");
        assertThat(capturedBindings.get())
                .contains("ranking_comp-1", "Copa", "TEAM", "LOWEST", 1, "user-1", 1700000000000L);
    }

    @Test
    void binds_the_event_ids_as_a_single_postgres_array() {
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        DSLContext dsl = DSL.using(
                new MockConnection(capturing(new AtomicReference<>(), capturedBindings)), SQLDialect.POSTGRES);
        new SaveRankingJooqAdapter(dsl).saveRanking(payload(RankingIncludeBy.LOWEST, 1));

        // The whole list travels as one binding, rendered by Postgres as {event-1,event-2}.
        assertThat(capturedBindings.get())
                .anySatisfy(binding -> assertThat(String.valueOf(binding)).contains("event-1", "event-2"));
    }

    @Test
    void binds_a_null_included_count_when_every_result_counts() {
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        DSLContext dsl = DSL.using(
                new MockConnection(capturing(new AtomicReference<>(), capturedBindings)), SQLDialect.POSTGRES);
        new SaveRankingJooqAdapter(dsl).saveRanking(payload(RankingIncludeBy.NONE, null));

        assertThat(capturedBindings.get()).contains("NONE").containsNull();
    }
}
