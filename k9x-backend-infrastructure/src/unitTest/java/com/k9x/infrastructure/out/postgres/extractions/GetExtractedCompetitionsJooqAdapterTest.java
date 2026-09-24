package com.k9x.infrastructure.out.postgres.extractions;

import com.k9x.application.extractions.use_case.dto.FetchExtractionLogEventDTO;
import com.k9x.application.extractions.use_case.dto.FetchExtractionLogStageDTO;
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

class GetExtractedCompetitionsJooqAdapterTest {

    private static final Field<?>[] STAGE_FIELDS = {
            Tables.STAGES.ID, Tables.STAGES.NAME, Tables.STAGES.DATE_FROM, Tables.STAGES.DATE_TO,
            Tables.COMPETITIONS.NAME, Tables.COMPETITIONS.COUNTRY, GetExtractedCompetitionsJooqAdapter.LOADED_AT
    };

    private static final Field<?>[] EVENT_FIELDS = {
            Tables.EVENTS.ID, Tables.EVENTS.NAME, Tables.EVENTS.DISCIPLINE, Tables.EVENTS.STAGE_ID,
            Tables.EVENTS.RANK_SCORE, EventProjectionFields.COMPETITOR_COUNT
    };

    private final List<String> sqls = new ArrayList<>();
    private final List<List<Object>> bindings = new ArrayList<>();

    private DSLContext dsl(boolean withStage) {
        MockDataProvider provider = ctx -> {
            String sql = ctx.sql().toLowerCase();
            sqls.add(sql);
            bindings.add(List.of(ctx.bindings()));
            DSLContext mock = DSL.using(SQLDialect.POSTGRES);
            if (sql.startsWith("select \"k9x\".\"stages\".\"id\"")) {
                Result<Record> result = mock.newResult(STAGE_FIELDS);
                if (withStage) {
                    Record r = mock.newRecord(STAGE_FIELDS);
                    r.set(Tables.STAGES.ID, "s-1");
                    r.set(Tables.STAGES.NAME, "Stage 1");
                    r.set(Tables.STAGES.DATE_FROM, 100L);
                    r.set(Tables.STAGES.DATE_TO, 200L);
                    r.set(Tables.COMPETITIONS.NAME, "Comp");
                    r.set(Tables.COMPETITIONS.COUNTRY, "ES");
                    r.set(GetExtractedCompetitionsJooqAdapter.LOADED_AT, 5_000L);
                    result.add(r);
                }
                return new MockResult[]{new MockResult(result.size(), result)};
            }
            if (sql.startsWith("select \"k9x\".\"events\".\"id\"")) {
                Result<Record> result = mock.newResult(EVENT_FIELDS);
                Record r = mock.newRecord(EVENT_FIELDS);
                r.set(Tables.EVENTS.ID, "e-1");
                r.set(Tables.EVENTS.NAME, "Event 1");
                r.set(Tables.EVENTS.DISCIPLINE, "OBDX");
                r.set(Tables.EVENTS.STAGE_ID, "s-1");
                r.set(Tables.EVENTS.RANK_SCORE, 900);
                r.set(EventProjectionFields.COMPETITOR_COUNT, 12);
                result.add(r);
                return new MockResult[]{new MockResult(1, result)};
            }
            return new MockResult[]{new MockResult(0, mock.newResult())};
        };
        return DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
    }

    @Test
    void reads_active_extracted_stages_with_their_latest_load_instant_in_sql() {
        List<FetchExtractionLogStageDTO> stages = new GetExtractedCompetitionsJooqAdapter(dsl(true)).getExtractedStages();

        assertThat(stages).containsExactly(new FetchExtractionLogStageDTO("s-1", "Stage 1", "Comp", "ES", 100L, 200L,
                5_000L, List.of(new FetchExtractionLogEventDTO("e-1", "Event 1", "OBDX", 12, 900))));
        assertThat(sqls.getFirst())
                .contains("\"k9x\".\"competitions\".\"source\" = ?")
                .contains("\"k9x\".\"competitions\".\"deleted_at\" is null")
                .contains("\"k9x\".\"stages\".\"deleted_at\" is null")
                .contains("distinct on")
                .contains("\"extraction_timestamp\" desc")
                .contains("\"latest_extraction\".\"created_at\" is not null");
        assertThat(bindings.getFirst()).contains("EXTRACTION");
    }

    @Test
    void counts_competitors_in_sql_and_never_loads_competitor_or_score_rows() {
        new GetExtractedCompetitionsJooqAdapter(dsl(true)).getExtractedStages();

        assertThat(sqls).hasSize(2);
        assertThat(sqls.get(1))
                .contains("count(*)")
                .contains("\"k9x\".\"events\".\"deleted_at\" is null")
                .doesNotContain("\"obdx\".\"event_scores\"");
    }

    @Test
    void skips_the_events_query_when_no_stage_was_extracted() {
        assertThat(new GetExtractedCompetitionsJooqAdapter(dsl(false)).getExtractedStages()).isEmpty();
        assertThat(sqls).hasSize(1);
    }
}
