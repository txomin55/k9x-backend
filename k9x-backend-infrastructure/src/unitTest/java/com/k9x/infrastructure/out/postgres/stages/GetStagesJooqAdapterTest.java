package com.k9x.infrastructure.out.postgres.stages;

import com.k9x.application.stages.use_case.dto.FetchStageListDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.Events;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GetStagesJooqAdapterTest {

    private static final Events E = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENTS;

    private static final Field<?>[] FIELDS = {
            Tables.STAGES.ID,
            Tables.STAGES.NAME,
            Tables.STAGES.DATE_FROM,
            Tables.STAGES.DATE_TO,
            Tables.COMPETITIONS.DESCRIPTION,
            Tables.COMPETITIONS.COUNTRY,
            Tables.COMPETITIONS.ADDRESS,
            Tables.COMPETITIONS.COORD_ALT,
            Tables.COMPETITIONS.COORD_LONG,
            DSL.field("organizer_name", String.class),
            DSL.field("event_id", String.class),
            DSL.field("event_name", String.class),
            E.CONFIGURATION_ID,
            DSL.field("competitor_count", Integer.class)
    };

    @Test
    void generates_sql_with_inner_joins_deleted_at_filter_and_date_from_order() {
        AtomicReference<String> capturedSql = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetStagesJooqAdapter(dsl).getStages();

        assertThat(capturedSql.get())
                .contains("from \"k9x\".\"stages\"")
                .contains("join \"k9x\".\"organizers\"")
                .contains("join \"obdx\".\"events\"")
                .doesNotContain("left outer join \"k9x\".\"organizers\"")
                .doesNotContain("left outer join \"obdx\".\"events\"")
                .contains("\"k9x\".\"stages\".\"deleted_at\" is null")
                .contains("\"obdx\".\"events\".\"deleted_at\" is null")
                .contains("order by \"k9x\".\"stages\".\"date_from\" asc");
    }

    @Test
    void maps_stage_with_one_event() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(FIELDS);
            Record r = mockDsl.newRecord(FIELDS);
            r.set(Tables.STAGES.ID, "stage-1");
            r.set(Tables.STAGES.NAME, "Stage A");
            r.set(Tables.STAGES.DATE_FROM, 1000L);
            r.set(Tables.STAGES.DATE_TO, 2000L);
            r.set(Tables.COMPETITIONS.DESCRIPTION, "desc");
            r.set(Tables.COMPETITIONS.COUNTRY, "ES");
            r.set(Tables.COMPETITIONS.ADDRESS, "Calle Mayor 1");
            r.set(Tables.COMPETITIONS.COORD_ALT, 40.4168);
            r.set(Tables.COMPETITIONS.COORD_LONG, -3.7038);
            r.set(DSL.field("organizer_name", String.class), "Organizer");
            r.set(DSL.field("event_id", String.class), "evt-1");
            r.set(DSL.field("event_name", String.class), "Open");
            r.set(E.CONFIGURATION_ID, "obdx-1");
            r.set(DSL.field("competitor_count", Integer.class), 5);
            result.add(r);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchStageListDTO> stages = new GetStagesJooqAdapter(dsl).getStages();

        assertThat(stages).hasSize(1);
        FetchStageListDTO stage = stages.getFirst();
        assertThat(stage.id()).isEqualTo("stage-1");
        assertThat(stage.name()).isEqualTo("Stage A");
        assertThat(stage.dateFrom()).isEqualTo(1000L);
        assertThat(stage.dateTo()).isEqualTo(2000L);
        assertThat(stage.organizer()).isEqualTo("Organizer");
        assertThat(stage.events()).hasSize(1);
        assertThat(stage.events().getFirst().id()).isEqualTo("evt-1");
        assertThat(stage.events().getFirst().name()).isEqualTo("Open");
        assertThat(stage.events().getFirst().configurationId()).isEqualTo("obdx-1");
        assertThat(stage.events().getFirst().competitorCount()).isEqualTo(5);
    }

    @Test
    void deduplicates_stage_with_multiple_events() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(FIELDS);

            Record r1 = mockDsl.newRecord(FIELDS);
            r1.set(Tables.STAGES.ID, "stage-1");
            r1.set(Tables.STAGES.NAME, "Stage A");
            r1.set(Tables.STAGES.DATE_FROM, 1000L);
            r1.set(Tables.STAGES.DATE_TO, 2000L);
            r1.set(Tables.COMPETITIONS.DESCRIPTION, "desc");
            r1.set(Tables.COMPETITIONS.COUNTRY, "ES");
            r1.set(Tables.COMPETITIONS.ADDRESS, "Calle Mayor 1");
            r1.set(DSL.field("organizer_name", String.class), "Organizer");
            r1.set(DSL.field("event_id", String.class), "evt-1");
            r1.set(DSL.field("event_name", String.class), "Open A");
            r1.set(E.CONFIGURATION_ID, "obdx-1");
            r1.set(DSL.field("competitor_count", Integer.class), 3);

            Record r2 = mockDsl.newRecord(FIELDS);
            r2.set(Tables.STAGES.ID, "stage-1");
            r2.set(Tables.STAGES.NAME, "Stage A");
            r2.set(Tables.STAGES.DATE_FROM, 1000L);
            r2.set(Tables.STAGES.DATE_TO, 2000L);
            r2.set(Tables.COMPETITIONS.DESCRIPTION, "desc");
            r2.set(Tables.COMPETITIONS.COUNTRY, "ES");
            r2.set(Tables.COMPETITIONS.ADDRESS, "Calle Mayor 1");
            r2.set(DSL.field("organizer_name", String.class), "Organizer");
            r2.set(DSL.field("event_id", String.class), "evt-2");
            r2.set(DSL.field("event_name", String.class), "Open B");
            r2.set(E.CONFIGURATION_ID, "obdx-2");
            r2.set(DSL.field("competitor_count", Integer.class), 7);

            result.add(r1);
            result.add(r2);
            return new MockResult[]{new MockResult(2, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchStageListDTO> stages = new GetStagesJooqAdapter(dsl).getStages();

        assertThat(stages).hasSize(1);
        assertThat(stages.getFirst().events()).hasSize(2);
    }
}
