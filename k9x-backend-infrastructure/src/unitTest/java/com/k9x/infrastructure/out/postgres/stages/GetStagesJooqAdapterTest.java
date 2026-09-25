package com.k9x.infrastructure.out.postgres.stages;

import com.k9x.application.stages.use_case.dto.FetchStageListRowDTO;
import com.k9x.application.stages.use_case.dto.FetchStageListRowEventDTO;
import com.k9x.infrastructure.out.postgres.events.EventProjectionFields;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetStagesJooqAdapterTest {

    private static final long TODAY = 1_800_000_000_000L;

    private static final Field<?>[] STAGE_FIELDS = {
            Tables.STAGES.ID, Tables.STAGES.NAME, Tables.STAGES.DATE_FROM, Tables.STAGES.DATE_TO,
            Tables.COMPETITIONS.ID, Tables.COMPETITIONS.NAME, Tables.COMPETITIONS.COUNTRY,
            Tables.COMPETITIONS.ADDRESS, Tables.COMPETITIONS.COORD_ALT, Tables.COMPETITIONS.COORD_LONG,
            GetStagesJooqAdapter.ORGANIZER_NAME
    };

    private static final Field<?>[] EVENT_FIELDS = {
            Tables.EVENTS.ID, Tables.EVENTS.NAME, Tables.EVENTS.DISCIPLINE, Tables.EVENTS.STAGE_ID,
            Tables.EVENTS.DELETED_AT, Tables.EVENTS.ENROLLMENT_DEADLINE, Tables.EVENTS.AWARDS,
            Tables.EVENTS.RANK_SCORE, EventProjectionFields.COMPETITOR_COUNT, EventProjectionFields.HAS_ANY_SCORE
    };

    private final List<String> sqls = new ArrayList<>();
    private final List<List<Object>> bindings = new ArrayList<>();

    /** Two stages: "past" finished before today, "running" ends after today; each with one event. */
    private DSLContext dsl() {
        MockDataProvider provider = ctx -> {
            String sql = ctx.sql().toLowerCase();
            sqls.add(sql);
            bindings.add(List.of(ctx.bindings()));
            DSLContext mock = DSL.using(SQLDialect.POSTGRES);
            if (sql.startsWith("select \"k9x\".\"stages\".\"id\"")) {
                Result<Record> result = mock.newResult(STAGE_FIELDS);
                result.add(stage(mock, "past", TODAY - 10, TODAY - 5));
                result.add(stage(mock, "running", TODAY - 1, TODAY + 5));
                return new MockResult[]{new MockResult(2, result)};
            }
            if (sql.startsWith("select \"k9x\".\"events\".\"id\", \"k9x\".\"events\".\"name\"")) {
                Result<Record> result = mock.newResult(EVENT_FIELDS);
                result.add(event(mock, "evt-past", "past", 12, true));
                result.add(event(mock, "evt-running", "running", 3, true));
                return new MockResult[]{new MockResult(2, result)};
            }
            return new MockResult[]{new MockResult(0, mock.newResult())};
        };
        return DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
    }

    private static Record stage(DSLContext mock, String id, long from, long to) {
        Record r = mock.newRecord(STAGE_FIELDS);
        r.set(Tables.STAGES.ID, id);
        r.set(Tables.STAGES.NAME, "Stage " + id);
        r.set(Tables.STAGES.DATE_FROM, from);
        r.set(Tables.STAGES.DATE_TO, to);
        r.set(Tables.COMPETITIONS.ID, "comp-1");
        r.set(Tables.COMPETITIONS.NAME, "Comp");
        r.set(GetStagesJooqAdapter.ORGANIZER_NAME, "Organizer");
        return r;
    }

    private static Record event(DSLContext mock, String id, String stageId, int competitors, boolean scored) {
        Record r = mock.newRecord(EVENT_FIELDS);
        r.set(Tables.EVENTS.ID, id);
        r.set(Tables.EVENTS.NAME, "Event " + id);
        r.set(Tables.EVENTS.DISCIPLINE, "obdx");
        r.set(Tables.EVENTS.STAGE_ID, stageId);
        r.set(EventProjectionFields.COMPETITOR_COUNT, competitors);
        r.set(EventProjectionFields.HAS_ANY_SCORE, scored);
        return r;
    }

    @Test
    void filters_the_date_range_and_deleted_rows_in_sql() {
        new GetStagesJooqAdapter(dsl()).getStages(100L, 200L, null, TODAY);

        assertThat(sqls.getFirst())
                .contains("join \"k9x\".\"competitions\"")
                .contains("\"k9x\".\"stages\".\"deleted_at\" is null")
                .contains("\"k9x\".\"competitions\".\"deleted_at\" is null")
                .contains("\"k9x\".\"stages\".\"date_from\" >= ?")
                .contains("\"k9x\".\"stages\".\"date_from\" <= ?");
    }

    @Test
    void leaves_an_open_range_bound_out_of_the_query() {
        new GetStagesJooqAdapter(dsl()).getStages(null, null, null, TODAY);

        assertThat(sqls.getFirst()).doesNotContain("\"date_from\" >=").doesNotContain("\"date_from\" <=");
    }

    @Test
    void filters_the_competition_country_in_sql_when_one_is_given() {
        new GetStagesJooqAdapter(dsl()).getStages(null, null, "ES", TODAY);

        assertThat(sqls.getFirst()).contains("\"k9x\".\"competitions\".\"country\" = ?");
        assertThat(bindings.getFirst()).contains("ES");
    }

    @Test
    void leaves_the_country_out_of_the_query_when_none_is_given() {
        new GetStagesJooqAdapter(dsl()).getStages(null, null, null, TODAY);

        assertThat(sqls.getFirst()).doesNotContain("\"country\" =");
    }

    @Test
    void aggregates_competitor_count_and_any_score_in_sql_instead_of_loading_them() {
        List<FetchStageListRowDTO> stages = new GetStagesJooqAdapter(dsl()).getStages(null, null, null, TODAY);

        String eventsSql = sqls.stream().filter(s -> s.startsWith("select \"k9x\".\"events\".\"id\", \"k9x\".\"events\".\"name\""))
                .findFirst().orElseThrow();
        assertThat(eventsSql)
                .contains("count(*)")
                .contains("exists")
                .contains("\"obdx\".\"event_scores\".\"score\" is not null");
        FetchStageListRowEventDTO past = stages.stream().filter(s -> s.id().equals("past")).findFirst().orElseThrow()
                .events().getFirst();
        assertThat(past.competitorCount()).isEqualTo(12);
        assertThat(past.hasAnyScore()).isTrue();
    }

    @Test
    void only_hydrates_the_events_whose_stage_has_not_finished_by_date() {
        new GetStagesJooqAdapter(dsl()).getStages(null, null, null, TODAY);

        // The hydrator's event query (it joins obdx.event_info) is the only one that can lead to score rows,
        // and it is scoped to the running event alone: the finished one is FINISHED by date.
        List<Integer> hydrations = new ArrayList<>();
        for (int i = 0; i < sqls.size(); i++) {
            if (sqls.get(i).contains("\"obdx\".\"event_info\"")) {
                hydrations.add(i);
            }
        }
        assertThat(hydrations).hasSize(1);
        assertThat(bindings.get(hydrations.getFirst())).contains("evt-running").doesNotContain("evt-past");
    }

    @Test
    void returns_nothing_without_touching_events_when_no_stage_matches() {
        MockDataProvider empty = ctx -> {
            sqls.add(ctx.sql());
            return new MockResult[]{new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult(STAGE_FIELDS))};
        };

        List<FetchStageListRowDTO> stages = new GetStagesJooqAdapter(
                DSL.using(new MockConnection(empty), SQLDialect.POSTGRES)).getStages(1L, 2L, null, TODAY);

        assertThat(stages).isEmpty();
        assertThat(sqls).hasSize(1);
    }
}
