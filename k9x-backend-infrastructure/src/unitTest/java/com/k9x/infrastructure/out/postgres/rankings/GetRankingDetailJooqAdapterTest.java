package com.k9x.infrastructure.out.postgres.rankings;

import com.k9x.application.rankings.use_case.dto.FetchRankingDTO;
import com.k9x.application.rankings.use_case.dto.FetchRankingEventDTO;
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

class GetRankingDetailJooqAdapterTest {

    private static final Field<?>[] FIELDS = {
            Tables.RANKINGS.ID,
            Tables.RANKINGS.NAME,
            Tables.RANKINGS.GROUP_BY,
            Tables.RANKINGS.INCLUDE_BY,
            Tables.RANKINGS.INCLUDED_COUNT,
            Tables.RANKINGS.INCLUDE_RESERVES,
            Tables.EVENTS.ID,
            Tables.EVENTS.NAME
    };

    private static Record row(DSLContext mockDsl, String eventId, String eventName) {
        Record record = mockDsl.newRecord(FIELDS);
        record.set(Tables.RANKINGS.ID, "ranking_comp-1");
        record.set(Tables.RANKINGS.NAME, "Copa");
        record.set(Tables.RANKINGS.GROUP_BY, "TEAM");
        record.set(Tables.RANKINGS.INCLUDE_BY, "LOWEST");
        record.set(Tables.RANKINGS.INCLUDED_COUNT, 2);
        record.set(Tables.RANKINGS.INCLUDE_RESERVES, true);
        record.set(Tables.EVENTS.ID, eventId);
        record.set(Tables.EVENTS.NAME, eventName);
        return record;
    }

    private static MockDataProvider returning(Record... records) {
        return _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(FIELDS);
            result.addAll(java.util.Arrays.asList(records));
            return new MockResult[]{new MockResult(records.length, result)};
        };
    }

    @Test
    void filters_by_id_and_creator_and_joins_events_on_the_array() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetRankingDetailJooqAdapter(dsl).getRankingDetail("ranking_comp-1", "user-1");

        assertThat(capturedSql.get())
                .contains("from \"k9x\".\"rankings\"")
                .contains("left outer join \"k9x\".\"events\"")
                .contains("any(")
                // The deleted_at filter belongs to the ON clause: in the WHERE it would drop rankings whose
                // events have all been deleted.
                .containsPattern("on \\(.*deleted_at\" is null\\)")
                .contains("\"creator\" = ?");
        assertThat(capturedBindings.get()).contains("ranking_comp-1", "user-1");
    }

    @Test
    void returns_null_when_the_ranking_does_not_exist() {
        DSLContext dsl = DSL.using(new MockConnection(returning()), SQLDialect.POSTGRES);

        assertThat(new GetRankingDetailJooqAdapter(dsl).getRankingDetail("ranking_comp-1", "user-1")).isNull();
    }

    @Test
    void folds_several_rows_into_one_ranking_with_all_its_events() {
        DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
        DSLContext dsl = DSL.using(new MockConnection(returning(
                row(mockDsl, "event-1", "Event 1"),
                row(mockDsl, "event-2", "Event 2"))), SQLDialect.POSTGRES);

        FetchRankingDTO ranking = new GetRankingDetailJooqAdapter(dsl)
                .getRankingDetail("ranking_comp-1", "user-1");

        assertThat(ranking.id()).isEqualTo("ranking_comp-1");
        assertThat(ranking.name()).isEqualTo("Copa");
        assertThat(ranking.groupBy()).isEqualTo("TEAM");
        assertThat(ranking.includeBy()).isEqualTo("LOWEST");
        assertThat(ranking.includedCount()).isEqualTo(2);
        assertThat(ranking.events())
                .extracting(FetchRankingEventDTO::id, FetchRankingEventDTO::name)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("event-1", "Event 1"),
                        org.assertj.core.groups.Tuple.tuple("event-2", "Event 2"));
    }

    @Test
    void returns_the_ranking_with_no_events_when_they_have_all_been_deleted() {
        DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
        // The left join yields a single row with null event columns.
        DSLContext dsl = DSL.using(
                new MockConnection(returning(row(mockDsl, null, null))), SQLDialect.POSTGRES);

        FetchRankingDTO ranking = new GetRankingDetailJooqAdapter(dsl)
                .getRankingDetail("ranking_comp-1", "user-1");

        assertThat(ranking).isNotNull();
        assertThat(ranking.events()).isEmpty();
    }
}
