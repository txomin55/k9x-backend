package com.k9x.infrastructure.out.postgres.rankings;

import com.k9x.domain.rankings.RankingIncludeBy;
import com.k9x.domain.rankings.RankingGroupBy;
import com.k9x.domain.rankings.aggregates.Ranking;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
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

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GetRankingJooqAdapterTest {

    private static final Field<?>[] FIELDS = Tables.RANKINGS.fields();

    @Test
    void generates_select_filtered_by_id() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetRankingJooqAdapter(dsl).getRanking("ranking_comp-1");

        assertThat(capturedSql.get())
                .contains("from \"k9x\".\"rankings\"")
                .contains("\"id\" = ?");
        assertThat(capturedBindings.get()).containsExactly("ranking_comp-1");
    }

    @Test
    void returns_null_when_there_is_no_row() {
        MockDataProvider provider = _ -> {
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);

        assertThat(new GetRankingJooqAdapter(dsl).getRanking("ranking_comp-1")).isNull();
    }

    @Test
    void maps_the_row_onto_the_aggregate() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(FIELDS);
            Record record = mockDsl.newRecord(FIELDS);
            record.set(Tables.RANKINGS.ID, "ranking_comp-1");
            record.set(Tables.RANKINGS.NAME, "Copa");
            record.set(Tables.RANKINGS.EVENT_IDS, new String[]{"event-1", "event-2"});
            record.set(Tables.RANKINGS.GROUP_BY, "TEAM");
            record.set(Tables.RANKINGS.INCLUDE_BY, "LOWEST");
            record.set(Tables.RANKINGS.INCLUDED_COUNT, 2);
            record.set(Tables.RANKINGS.INCLUDE_RESERVES, true);
            record.set(Tables.RANKINGS.CREATOR, "user-1");
            record.set(Tables.RANKINGS.CREATED_AT, 1700000000000L);
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        Ranking ranking = new GetRankingJooqAdapter(dsl).getRanking("ranking_comp-1");

        assertThat(ranking.id()).isEqualTo("ranking_comp-1");
        assertThat(ranking.name()).isEqualTo("Copa");
        assertThat(ranking.eventIds()).containsExactly("event-1", "event-2");
        assertThat(ranking.groupBy()).isEqualTo(RankingGroupBy.TEAM);
        assertThat(ranking.includeBy()).isEqualTo(RankingIncludeBy.LOWEST);
        assertThat(ranking.includedCount()).isEqualTo(2);
        assertThat(ranking.creator()).isEqualTo("user-1");
        assertThat(ranking.createdAt()).isEqualTo(1700000000000L);
    }
}
