package com.k9x.infrastructure.out.postgres.stages;

import com.k9x.application.stages.use_case.dto.FetchStageDetailDTO;
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

class GetStageDetailJooqAdapterTest {

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
            Tables.EVENTS.DISCIPLINE,
            Tables.EVENTS.CONFIGURATION_ID,
            Tables.DOGS.ID,
            DSL.field("dog_name", String.class),
            Tables.DOGS.OWNER,
            Tables.DOGS.COUNTRY,
            Tables.DOGS.TEAM,
            Tables.DOGS.BREED
    };

    @Test
    void generates_sql_with_inner_joins_on_competition_and_organizer_and_left_joins_on_events_and_competitors() {
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
                .contains("left outer join \"k9x\".\"events\"")
                .contains("left outer join \"obdx\".\"event_competitors\"")
                .contains("left outer join \"k9x\".\"dogs\"")
                .doesNotContain("left outer join \"k9x\".\"competitions\"")
                .doesNotContain("left outer join \"k9x\".\"organizers\"")
                .contains("\"k9x\".\"events\".\"deleted_at\" is null")
                .contains("\"k9x\".\"dogs\".\"deleted_at\" is null")
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
    void maps_stage_with_one_event_and_its_competitors() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(FIELDS);
            result.add(eventCompetitorRecord(mockDsl, "evt-1", "Open", "obdx", "obdx-1",
                    "dog-1", "Rex", "Alice", "ES", "Team A", "Border Collie"));
            result.add(eventCompetitorRecord(mockDsl, "evt-1", "Open", "obdx", "obdx-1",
                    "dog-2", "Fido", "Bob", "FR", "Team B", "Labrador"));
            return new MockResult[]{new MockResult(2, result)};
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
        assertThat(stage.events().getFirst().disciplineId()).isEqualTo("obdx");
        assertThat(stage.events().getFirst().configurationId()).isEqualTo("obdx-1");
        assertThat(stage.events().getFirst().competitors()).hasSize(2);
        assertThat(stage.events().getFirst().competitors().getFirst().dogId()).isEqualTo("dog-1");
        assertThat(stage.events().getFirst().competitors().getFirst().dogName()).isEqualTo("Rex");
        assertThat(stage.events().getFirst().competitors().getFirst().owner()).isEqualTo("Alice");
        assertThat(stage.events().getFirst().competitors().getFirst().country()).isEqualTo("ES");
        assertThat(stage.events().getFirst().competitors().getFirst().team()).isEqualTo("Team A");
        assertThat(stage.events().getFirst().competitors().getFirst().breed()).isEqualTo("Border Collie");
    }

    @Test
    void maps_event_with_no_competitors_when_left_join_returns_no_match() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(FIELDS);
            result.add(eventCompetitorRecord(mockDsl, "evt-1", "Open", "obdx", "obdx-1",
                    null, null, null, null, null, null));
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        FetchStageDetailDTO stage = new GetStageDetailJooqAdapter(dsl).getStage("stage-1");

        assertThat(stage.events()).hasSize(1);
        assertThat(stage.events().getFirst().competitors()).isEmpty();
    }

    @Test
    void maps_stage_with_no_events_when_left_join_returns_no_match() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(FIELDS);
            result.add(eventCompetitorRecord(mockDsl, null, null, null, null,
                    null, null, null, null, null, null));
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
            Record r = eventCompetitorRecord(mockDsl, null, null, null, null,
                    null, null, null, null, null, null);
            r.set(Tables.STAGES.DELETED_AT, 9999L);
            result.add(r);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        FetchStageDetailDTO stage = new GetStageDetailJooqAdapter(dsl).getStage("stage-1");

        assertThat(stage.deletedAt()).isEqualTo(9999L);
    }

    private static Record eventCompetitorRecord(DSLContext mockDsl, String eventId, String eventName,
                                                String discipline, String configurationId,
                                                String dogId, String dogName, String owner,
                                                String country, String team, String breed) {
        Record r = mockDsl.newRecord(FIELDS);
        r.set(Tables.STAGES.ID, "stage-1");
        r.set(Tables.STAGES.NAME, "Stage A");
        r.set(Tables.STAGES.DATE_FROM, 1000L);
        r.set(Tables.STAGES.DATE_TO, 2000L);
        r.set(Tables.STAGES.DELETED_AT, null);
        r.set(Tables.COMPETITIONS.ADDRESS, "Calle Mayor 1");
        r.set(DSL.field("organizer_name", String.class), "Organizer");
        r.set(DSL.field("event_id", String.class), eventId);
        r.set(DSL.field("event_name", String.class), eventName);
        r.set(Tables.EVENTS.DISCIPLINE, discipline);
        r.set(Tables.EVENTS.CONFIGURATION_ID, configurationId);
        r.set(Tables.DOGS.ID, dogId);
        r.set(DSL.field("dog_name", String.class), dogName);
        r.set(Tables.DOGS.OWNER, owner);
        r.set(Tables.DOGS.COUNTRY, country);
        r.set(Tables.DOGS.TEAM, team);
        r.set(Tables.DOGS.BREED, breed);
        return r;
    }
}
