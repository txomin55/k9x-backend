package com.k9x.infrastructure.out.postgres.stages;

import com.k9x.application.stages.use_case.dto.FetchStageDetailCompetitorDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailRowDTO;
import com.k9x.infrastructure.out.postgres.events.EventProjectionFields;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventCompetitors;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GetStageDetailJooqAdapterTest {

    private static final long TODAY = 1_800_000_000_000L;
    private static final EventCompetitors EC =
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS;

    private static final Field<?>[] STAGE_FIELDS = {
            Tables.STAGES.ID, Tables.STAGES.NAME, Tables.STAGES.DATE_FROM, Tables.STAGES.DATE_TO,
            Tables.STAGES.DELETED_AT, Tables.COMPETITIONS.ID, Tables.COMPETITIONS.NAME, Tables.COMPETITIONS.ADDRESS,
            GetStageDetailJooqAdapter.ORGANIZER_NAME
    };
    private static final Field<?>[] EVENT_FIELDS = {
            Tables.EVENTS.ID, Tables.EVENTS.NAME, Tables.EVENTS.DISCIPLINE,
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_INFO.CONFIGURATION_ID,
            Tables.EVENTS.DELETED_AT, Tables.EVENTS.ENROLLMENT_DEADLINE, Tables.EVENTS.AWARDS,
            Tables.EVENTS.RANK_SCORE, EventProjectionFields.HAS_ANY_SCORE
    };
    private static final Field<?>[] COMPETITOR_FIELDS = {
            EC.EVENT_ID, EC.DOG_IDENTIFICATION, EC.VERIFIED, EC.HANDLER, EC.TEAM, EC.COUNTRY,
            Tables.DOGS.NAME, Tables.DOGS.OWNER, Tables.DOGS.BREED, GetStageDetailJooqAdapter.DOG_HANDLER,
            GetStageDetailJooqAdapter.DOG_TEAM, GetStageDetailJooqAdapter.DOG_COUNTRY
    };

    private final List<String> sqls = new ArrayList<>();

    private DSLContext dsl(boolean stageExists, long dateTo) {
        MockDataProvider provider = ctx -> {
            String sql = ctx.sql().toLowerCase();
            sqls.add(sql);
            DSLContext mock = DSL.using(SQLDialect.POSTGRES);
            if (sql.startsWith("select \"k9x\".\"stages\".\"id\"")) {
                Result<Record> result = mock.newResult(STAGE_FIELDS);
                if (stageExists) {
                    Record r = mock.newRecord(STAGE_FIELDS);
                    r.set(Tables.STAGES.ID, "s-1");
                    r.set(Tables.STAGES.NAME, "Stage 1");
                    r.set(Tables.STAGES.DATE_FROM, dateTo - 10);
                    r.set(Tables.STAGES.DATE_TO, dateTo);
                    r.set(Tables.COMPETITIONS.ID, "comp-1");
                    r.set(Tables.COMPETITIONS.NAME, "Comp");
                    result.add(r);
                }
                return new MockResult[]{new MockResult(result.size(), result)};
            }
            if (sql.startsWith("select \"k9x\".\"events\".\"id\", \"k9x\".\"events\".\"name\"")) {
                Result<Record> result = mock.newResult(EVENT_FIELDS);
                Record r = mock.newRecord(EVENT_FIELDS);
                r.set(Tables.EVENTS.ID, "e-1");
                r.set(Tables.EVENTS.NAME, "Event 1");
                r.set(EventProjectionFields.HAS_ANY_SCORE, true);
                result.add(r);
                return new MockResult[]{new MockResult(1, result)};
            }
            if (sql.startsWith("select \"obdx\".\"event_competitors\".\"event_id\", \"obdx\".\"event_competitors\".\"dog_identification\", \"obdx\".\"event_competitors\".\"verified\"")) {
                Result<Record> result = mock.newResult(COMPETITOR_FIELDS);
                Record r = mock.newRecord(COMPETITOR_FIELDS);
                r.set(EC.EVENT_ID, "e-1");
                r.set(EC.DOG_IDENTIFICATION, "dog-1");
                r.set(EC.VERIFIED, true);
                r.set(EC.HANDLER, " ");                       // blank snapshot -> falls back to the dog
                r.set(EC.COUNTRY, "FR");                      // snapshot wins over the dog
                r.set(Tables.DOGS.NAME, "Rex");
                r.set(GetStageDetailJooqAdapter.DOG_HANDLER, "Dog Handler");
                r.set(GetStageDetailJooqAdapter.DOG_COUNTRY, "ES");
                result.add(r);
                return new MockResult[]{new MockResult(1, result)};
            }
            return new MockResult[]{new MockResult(0, mock.newResult())};
        };
        return DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
    }

    @Test
    void returns_empty_when_the_stage_does_not_exist() {
        assertThat(new GetStageDetailJooqAdapter(dsl(false, TODAY)).getStage("s-1", TODAY)).isEmpty();
        assertThat(sqls).hasSize(1);
    }

    @Test
    void reads_only_the_requested_stage_and_aggregates_any_score_in_sql() {
        Optional<FetchStageDetailRowDTO> stage = new GetStageDetailJooqAdapter(dsl(true, TODAY - 5)).getStage("s-1", TODAY);

        assertThat(stage).isPresent();
        assertThat(stage.get().events().getFirst().hasAnyScore()).isTrue();
        assertThat(sqls.getFirst()).contains("\"k9x\".\"stages\".\"id\" = ?");
        assertThat(sqls).noneMatch(s -> s.contains("\"k9x\".\"competitions\".\"id\" in"));
    }

    @Test
    void maps_competitors_preferring_the_frozen_snapshot_over_the_dog() {
        FetchStageDetailCompetitorDTO competitor = new GetStageDetailJooqAdapter(dsl(true, TODAY - 5))
                .getStage("s-1", TODAY).orElseThrow().events().getFirst().competitors().getFirst();

        assertThat(competitor).isEqualTo(new FetchStageDetailCompetitorDTO("dog-1", "Rex", null, "Dog Handler", "FR",
                null, null, true));
    }

    @Test
    void a_stage_finished_by_date_never_loads_scores() {
        new GetStageDetailJooqAdapter(dsl(true, TODAY - 5)).getStage("s-1", TODAY);

        assertThat(sqls).noneMatch(s -> s.contains("\"obdx\".\"event_info\"") && s.contains("\"k9x\".\"events\".\"id\" in"));
    }

    @Test
    void a_running_stage_hydrates_its_events_with_competitors_to_ask_if_they_are_settled() {
        new GetStageDetailJooqAdapter(dsl(true, TODAY + 5)).getStage("s-1", TODAY);

        assertThat(sqls).anyMatch(s -> s.contains("\"obdx\".\"event_info\"") && s.contains("\"k9x\".\"events\".\"id\" in"));
    }
}
