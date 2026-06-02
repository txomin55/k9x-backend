package com.k9x.infrastructure.out.postgres.competitions;

import com.k9x.application.competitions.use_case.dto.FetchCompetitionDTO;
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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class GetCompetitionListJooqAdapterTest {

    private static final Field<?>[] JOIN_FIELDS = Stream.of(
            Arrays.stream(Tables.COMPETITIONS.fields()),
            Arrays.stream(Tables.STAGES.fields()),
            Arrays.stream(Tables.EVENTS.fields())
    ).flatMap(s -> s).toArray(Field[]::new);

    @Test
    void generates_sql_with_left_join_filtered_by_creator_and_not_deleted() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(JOIN_FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetCompetitionListJooqAdapter(dsl).getCompetitions("creator-1");

        assertThat(capturedSql.get())
                .contains("from \"k9x\".\"competitions\"")
                .contains("left outer join \"k9x\".\"stages\"")
                .contains("\"k9x\".\"stages\".\"deleted_at\" is null")
                .contains("left outer join \"k9x\".\"events\"")
                .contains("\"k9x\".\"events\".\"deleted_at\" is null")
                .contains("\"k9x\".\"competitions\".\"creator\" = ?")
                .contains("\"k9x\".\"competitions\".\"deleted_at\" is null");
        assertThat(capturedBindings.get()).containsExactly("creator-1");
    }

    @Test
    void maps_competition_with_one_stage() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(JOIN_FIELDS);
            Record record = mockDsl.newRecord(JOIN_FIELDS);
            record.set(Tables.COMPETITIONS.ID, "comp-1");
            record.set(Tables.COMPETITIONS.NAME, "World Cup");
            record.set(Tables.COMPETITIONS.DESCRIPTION, "desc");
            record.set(Tables.COMPETITIONS.COUNTRY, "ES");
            record.set(Tables.COMPETITIONS.ADDRESS, "Madrid");
            record.set(Tables.COMPETITIONS.CREATOR, "creator-1");
            record.set(Tables.STAGES.ID, "stage-1");
            record.set(Tables.STAGES.NAME, "Stage A");
            record.set(Tables.STAGES.DATE_FROM, 1000L);
            record.set(Tables.STAGES.DATE_TO, 2000L);
            record.set(Tables.STAGES.COMPETITION_ID, "comp-1");
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchCompetitionDTO> competitions = new GetCompetitionListJooqAdapter(dsl).getCompetitions("creator-1");

        assertThat(competitions).hasSize(1);
        FetchCompetitionDTO comp = competitions.getFirst();
        assertThat(comp.id()).isEqualTo("comp-1");
        assertThat(comp.name()).isEqualTo("World Cup");
        assertThat(comp.stages()).hasSize(1);
        assertThat(comp.stages().getFirst().id()).isEqualTo("stage-1");
        assertThat(comp.stages().getFirst().name()).isEqualTo("Stage A");
        assertThat(comp.stages().getFirst().dateFrom()).isEqualTo(1000L);
        assertThat(comp.stages().getFirst().dateTo()).isEqualTo(2000L);
    }

    @Test
    void maps_stage_with_its_non_deleted_events() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(JOIN_FIELDS);

            Record r1 = mockDsl.newRecord(JOIN_FIELDS);
            r1.set(Tables.COMPETITIONS.ID, "comp-1");
            r1.set(Tables.COMPETITIONS.NAME, "World Cup");
            r1.set(Tables.COMPETITIONS.CREATOR, "creator-1");
            r1.set(Tables.STAGES.ID, "stage-1");
            r1.set(Tables.STAGES.NAME, "Stage A");
            r1.set(Tables.STAGES.DATE_FROM, 1000L);
            r1.set(Tables.STAGES.DATE_TO, 2000L);
            r1.set(Tables.STAGES.COMPETITION_ID, "comp-1");
            r1.set(Tables.EVENTS.ID, "event-1");
            r1.set(Tables.EVENTS.NAME, "Event One");
            r1.set(Tables.EVENTS.DISCIPLINE, "OBDX");
            r1.set(Tables.EVENTS.STAGE_ID, "stage-1");

            Record r2 = mockDsl.newRecord(JOIN_FIELDS);
            r2.set(Tables.COMPETITIONS.ID, "comp-1");
            r2.set(Tables.COMPETITIONS.NAME, "World Cup");
            r2.set(Tables.COMPETITIONS.CREATOR, "creator-1");
            r2.set(Tables.STAGES.ID, "stage-1");
            r2.set(Tables.STAGES.NAME, "Stage A");
            r2.set(Tables.STAGES.DATE_FROM, 1000L);
            r2.set(Tables.STAGES.DATE_TO, 2000L);
            r2.set(Tables.STAGES.COMPETITION_ID, "comp-1");
            r2.set(Tables.EVENTS.ID, "event-2");
            r2.set(Tables.EVENTS.NAME, "Event Two");
            r2.set(Tables.EVENTS.DISCIPLINE, "OBDX");
            r2.set(Tables.EVENTS.STAGE_ID, "stage-1");

            result.add(r1);
            result.add(r2);
            return new MockResult[]{new MockResult(2, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchCompetitionDTO> competitions = new GetCompetitionListJooqAdapter(dsl).getCompetitions("creator-1");

        assertThat(competitions).hasSize(1);
        assertThat(competitions.getFirst().stages()).hasSize(1);
        var stage = competitions.getFirst().stages().getFirst();
        assertThat(stage.events()).hasSize(2);
        assertThat(stage.events().getFirst().id()).isEqualTo("event-1");
        assertThat(stage.events().getFirst().name()).isEqualTo("Event One");
        assertThat(stage.events().getFirst().discipline()).isEqualTo("OBDX");
        assertThat(stage.events().get(1).id()).isEqualTo("event-2");
    }

    @Test
    void maps_stage_with_empty_events_when_left_join_returns_no_match() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(JOIN_FIELDS);
            Record record = mockDsl.newRecord(JOIN_FIELDS);
            record.set(Tables.COMPETITIONS.ID, "comp-1");
            record.set(Tables.COMPETITIONS.NAME, "World Cup");
            record.set(Tables.COMPETITIONS.CREATOR, "creator-1");
            record.set(Tables.STAGES.ID, "stage-1");
            record.set(Tables.STAGES.NAME, "Stage A");
            record.set(Tables.STAGES.DATE_FROM, 1000L);
            record.set(Tables.STAGES.DATE_TO, 2000L);
            record.set(Tables.STAGES.COMPETITION_ID, "comp-1");
            record.set(Tables.EVENTS.ID, null);
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchCompetitionDTO> competitions = new GetCompetitionListJooqAdapter(dsl).getCompetitions("creator-1");

        assertThat(competitions).hasSize(1);
        assertThat(competitions.getFirst().stages()).hasSize(1);
        assertThat(competitions.getFirst().stages().getFirst().events()).isEmpty();
    }

    @Test
    void deduplicates_competition_with_multiple_stages() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(JOIN_FIELDS);

            Record r1 = mockDsl.newRecord(JOIN_FIELDS);
            r1.set(Tables.COMPETITIONS.ID, "comp-1");
            r1.set(Tables.COMPETITIONS.NAME, "World Cup");
            r1.set(Tables.COMPETITIONS.DESCRIPTION, "desc");
            r1.set(Tables.COMPETITIONS.COUNTRY, "ES");
            r1.set(Tables.COMPETITIONS.ADDRESS, "Madrid");
            r1.set(Tables.COMPETITIONS.CREATOR, "creator-1");
            r1.set(Tables.STAGES.ID, "stage-1");
            r1.set(Tables.STAGES.NAME, "Stage A");
            r1.set(Tables.STAGES.DATE_FROM, 1000L);
            r1.set(Tables.STAGES.DATE_TO, 2000L);
            r1.set(Tables.STAGES.COMPETITION_ID, "comp-1");

            Record r2 = mockDsl.newRecord(JOIN_FIELDS);
            r2.set(Tables.COMPETITIONS.ID, "comp-1");
            r2.set(Tables.COMPETITIONS.NAME, "World Cup");
            r2.set(Tables.COMPETITIONS.DESCRIPTION, "desc");
            r2.set(Tables.COMPETITIONS.COUNTRY, "ES");
            r2.set(Tables.COMPETITIONS.ADDRESS, "Madrid");
            r2.set(Tables.COMPETITIONS.CREATOR, "creator-1");
            r2.set(Tables.STAGES.ID, "stage-2");
            r2.set(Tables.STAGES.NAME, "Stage B");
            r2.set(Tables.STAGES.DATE_FROM, 3000L);
            r2.set(Tables.STAGES.DATE_TO, 4000L);
            r2.set(Tables.STAGES.COMPETITION_ID, "comp-1");

            result.add(r1);
            result.add(r2);
            return new MockResult[]{new MockResult(2, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchCompetitionDTO> competitions = new GetCompetitionListJooqAdapter(dsl).getCompetitions("creator-1");

        assertThat(competitions).hasSize(1);
        assertThat(competitions.getFirst().stages()).hasSize(2);
    }

    @Test
    void maps_competition_with_empty_stages_when_left_join_returns_no_match() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(JOIN_FIELDS);
            Record record = mockDsl.newRecord(JOIN_FIELDS);
            record.set(Tables.COMPETITIONS.ID, "comp-1");
            record.set(Tables.COMPETITIONS.NAME, "World Cup");
            record.set(Tables.COMPETITIONS.DESCRIPTION, "desc");
            record.set(Tables.COMPETITIONS.COUNTRY, "ES");
            record.set(Tables.COMPETITIONS.ADDRESS, "Madrid");
            record.set(Tables.COMPETITIONS.CREATOR, "creator-1");
            record.set(Tables.STAGES.ID, null);
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchCompetitionDTO> competitions = new GetCompetitionListJooqAdapter(dsl).getCompetitions("creator-1");

        assertThat(competitions).hasSize(1);
        assertThat(competitions.getFirst().stages()).isEmpty();
    }
}
