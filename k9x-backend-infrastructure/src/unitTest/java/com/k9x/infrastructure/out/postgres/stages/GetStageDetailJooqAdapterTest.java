package com.k9x.infrastructure.out.postgres.stages;

import com.k9x.application.stages.use_case.dto.FetchStageDetailDTO;
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

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GetStageDetailJooqAdapterTest {

    private static final Events E = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENTS;

    private static final Field<?>[] FIELDS = {
            Tables.STAGES.ID,
            Tables.STAGES.NAME,
            Tables.STAGES.DATE_FROM,
            Tables.STAGES.DATE_TO,
            Tables.STAGES.DELETED_AT,
            Tables.COMPETITIONS.ADDRESS,
            DSL.field("organizer_name", String.class),
            DSL.field("event_id", String.class),
            DSL.field("event_name", String.class),
            E.CONFIGURATION_ID
    };

    @Test
    void generates_sql_with_inner_joins_on_competition_and_organizer_and_left_join_on_events() {
        AtomicReference<String> capturedSql = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetStageDetailJooqAdapter(dsl).getStage("stage-1");

        assertThat(capturedSql.get())
                .contains("from \"k9x\".\"stages\"")
                .contains("join \"k9x\".\"competitions\"")
                .contains("join \"k9x\".\"organizers\"")
                .contains("left outer join \"obdx\".\"events\"")
                .doesNotContain("left outer join \"k9x\".\"competitions\"")
                .doesNotContain("left outer join \"k9x\".\"organizers\"")
                .contains("\"obdx\".\"events\".\"deleted_at\" is null")
                .contains("\"k9x\".\"stages\".\"id\" = ?");
    }

    @Test
    void returns_null_when_stage_not_found() {
        MockDataProvider provider = _ -> {
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        FetchStageDetailDTO result = new GetStageDetailJooqAdapter(dsl).getStage("stage-1");

        assertThat(result).isNull();
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
            r.set(Tables.STAGES.DELETED_AT, null);
            r.set(Tables.COMPETITIONS.ADDRESS, "Calle Mayor 1");
            r.set(DSL.field("organizer_name", String.class), "Organizer");
            r.set(DSL.field("event_id", String.class), "evt-1");
            r.set(DSL.field("event_name", String.class), "Open");
            r.set(E.CONFIGURATION_ID, "obdx-1");
            result.add(r);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        FetchStageDetailDTO stage = new GetStageDetailJooqAdapter(dsl).getStage("stage-1");

        assertThat(stage.id()).isEqualTo("stage-1");
        assertThat(stage.name()).isEqualTo("Stage A");
        assertThat(stage.dateFrom()).isEqualTo(1000L);
        assertThat(stage.dateTo()).isEqualTo(2000L);
        assertThat(stage.address()).isEqualTo("Calle Mayor 1");
        assertThat(stage.organizer()).isEqualTo("Organizer");
        assertThat(stage.deletedAt()).isNull();
        assertThat(stage.events()).hasSize(1);
        assertThat(stage.events().getFirst().id()).isEqualTo("evt-1");
        assertThat(stage.events().getFirst().name()).isEqualTo("Open");
        assertThat(stage.events().getFirst().configurationId()).isEqualTo("obdx-1");
    }

    @Test
    void maps_stage_with_no_events_when_left_join_returns_no_match() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(FIELDS);
            Record r = mockDsl.newRecord(FIELDS);
            r.set(Tables.STAGES.ID, "stage-1");
            r.set(Tables.STAGES.NAME, "Stage A");
            r.set(Tables.STAGES.DATE_FROM, 1000L);
            r.set(Tables.STAGES.DATE_TO, 2000L);
            r.set(Tables.STAGES.DELETED_AT, null);
            r.set(Tables.COMPETITIONS.ADDRESS, "Calle Mayor 1");
            r.set(DSL.field("organizer_name", String.class), "Organizer");
            r.set(DSL.field("event_id", String.class), null);
            result.add(r);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        FetchStageDetailDTO stage = new GetStageDetailJooqAdapter(dsl).getStage("stage-1");

        assertThat(stage).isNotNull();
        assertThat(stage.events()).isEmpty();
    }

    @Test
    void maps_deleted_at_field() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(FIELDS);
            Record r = mockDsl.newRecord(FIELDS);
            r.set(Tables.STAGES.ID, "stage-1");
            r.set(Tables.STAGES.NAME, "Stage A");
            r.set(Tables.STAGES.DATE_FROM, 1000L);
            r.set(Tables.STAGES.DATE_TO, 2000L);
            r.set(Tables.STAGES.DELETED_AT, 9999L);
            r.set(Tables.COMPETITIONS.ADDRESS, "Calle Mayor 1");
            r.set(DSL.field("organizer_name", String.class), "Organizer");
            r.set(DSL.field("event_id", String.class), null);
            result.add(r);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        FetchStageDetailDTO stage = new GetStageDetailJooqAdapter(dsl).getStage("stage-1");

        assertThat(stage.deletedAt()).isEqualTo(9999L);
    }
}
