package com.k9x.infrastructure.out.postgres.competitions;

import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.dogs.aggregates.Sex;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The {@code getCompetition} call hydrates the full tree through {@link CompetitionHydrator},
 * which issues several sequential SELECTs (competitions, stages, events, competitors, exercises,
 * judges, scores). The {@link MockDataProvider} routes by the FROM table referenced in the SQL,
 * returning an empty result for the tables a given test does not care about.
 */
class GetCompetitionJooqAdapterTest {

    private static final Field<String> ORGANIZER_NAME = Tables.ORGANIZERS.NAME.as("organizer_name");

    private static final Field<?>[] COMPETITION_FIELDS = {
            Tables.COMPETITIONS.ID,
            Tables.COMPETITIONS.NAME,
            Tables.COMPETITIONS.CREATOR,
            ORGANIZER_NAME,
            Tables.COMPETITIONS.COUNTRY,
            Tables.COMPETITIONS.DESCRIPTION,
            Tables.COMPETITIONS.ADDRESS,
            Tables.COMPETITIONS.COORD_ALT,
            Tables.COMPETITIONS.COORD_LONG,
            Tables.COMPETITIONS.LAST_UPDATE,
            Tables.COMPETITIONS.CREATED_AT,
            Tables.COMPETITIONS.DELETED_AT
    };

    private static final Field<?>[] STAGE_FIELDS = {
            Tables.STAGES.ID,
            Tables.STAGES.NAME,
            Tables.STAGES.COMPETITION_ID,
            Tables.STAGES.CREATOR,
            Tables.STAGES.DATE_FROM,
            Tables.STAGES.DATE_TO,
            Tables.STAGES.LAST_UPDATE,
            Tables.STAGES.CREATED_AT,
            Tables.STAGES.DELETED_AT
    };

    private static final Field<?>[] EVENT_FIELDS = {
            Tables.EVENTS.ID,
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_INFO.CONFIGURATION_ID,
            Tables.EVENTS.DISCIPLINE,
            Tables.EVENTS.NAME,
            Tables.EVENTS.STAGE_ID,
            Tables.EVENTS.CREATOR,
            Tables.EVENTS.ENROLLMENT_DEADLINE,
            Tables.EVENTS.LAST_UPDATE,
            Tables.EVENTS.CREATED_AT,
            Tables.EVENTS.DELETED_AT,
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_INFO.SCORE_CALCULATION,
            Tables.EVENTS.AWARDS,
            Tables.EVENTS.RANK_SCORE,
            Tables.EVENTS.INTERNATIONAL,
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_INFO.COMMISSIONER,
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_INFO.CATEGORY
    };

    private static final Field<?>[] COMPETITION_ID_FIELDS = {Tables.STAGES.COMPETITION_ID};

    private static final Field<?>[] COMPETITOR_FIELDS = {
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS.EVENT_ID,
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS.DOG_IDENTIFICATION,
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS.START_NUMBER,
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS.COMPETITOR_NUMBER,
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS.VERIFIED,
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS.NOT_COMPETING,
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS.BIH,
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS.PRIMER,
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS.RESERVE,
            Tables.DOGS.NAME,
            Tables.DOGS.OWNER,
            Tables.DOGS.HANDLER,
            Tables.DOGS.TEAM,
            Tables.DOGS.COUNTRY,
            Tables.DOGS.BREED,
            Tables.DOGS.ORIGIN,
            Tables.DOGS.LICENSE,
            Tables.DOGS.SEX,
            Tables.DOGS.THREE_FCI_GENERATIONS_CONFIRMED
    };

    private static MockResult emptyNoFields() {
        return new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult());
    }

    /**
     * Routes a hydrator query by its FROM table. Child tables prefixed {@code event_} are matched
     * BEFORE the bare {@code events}/{@code competitions} substrings so they do not collide.
     */
    private static MockResult routeHydrator(MockExecuteContext ctx, Result<Record> competitions,
                                            Result<Record> stages, Result<Record> events) {
        String sql = ctx.sql().toLowerCase();
        if (sql.contains("event_competitors")) {
            return emptyNoFields();
        }
        if (sql.contains("event_exercises")) {
            return emptyNoFields();
        }
        if (sql.contains("event_judges")) {
            return emptyNoFields();
        }
        if (sql.contains("event_scores")) {
            return emptyNoFields();
        }
        if (sql.contains("from \"k9x\".\"events\"") || sql.contains("from events")) {
            return new MockResult(events.size(), events);
        }
        if (sql.contains("from \"k9x\".\"stages\"") || sql.contains("from stages")) {
            return new MockResult(stages.size(), stages);
        }
        if (sql.contains("competitions")) {
            return new MockResult(competitions.size(), competitions);
        }
        return emptyNoFields();
    }

    @Test
    void returns_null_when_competition_not_found() {
        MockDataProvider provider = ctx -> {
            DSLContext mock = DSL.using(SQLDialect.POSTGRES);
            return new MockResult[]{routeHydrator(ctx,
                    mock.newResult(COMPETITION_FIELDS),
                    mock.newResult(STAGE_FIELDS),
                    mock.newResult(EVENT_FIELDS))};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        CompetitionSnapshot competition = new GetCompetitionJooqAdapter(dsl).getCompetition("comp-123");

        assertThat(competition).isNull();
    }

    @Test
    void hydrates_competition_with_one_stage_and_one_event() {
        MockDataProvider provider = ctx -> {
            DSLContext mock = DSL.using(SQLDialect.POSTGRES);

            Result<Record> competitions = mock.newResult(COMPETITION_FIELDS);
            Record comp = mock.newRecord(COMPETITION_FIELDS);
            comp.set(Tables.COMPETITIONS.ID, "comp-1");
            comp.set(Tables.COMPETITIONS.NAME, "World Cup");
            comp.set(Tables.COMPETITIONS.CREATOR, "creator-1");
            comp.set(ORGANIZER_NAME, "Acme Org");
            comp.set(Tables.COMPETITIONS.COUNTRY, "ES");
            comp.set(Tables.COMPETITIONS.LAST_UPDATE, 10L);
            comp.set(Tables.COMPETITIONS.CREATED_AT, 20L);
            competitions.add(comp);

            Result<Record> stages = mock.newResult(STAGE_FIELDS);
            Record stage = mock.newRecord(STAGE_FIELDS);
            stage.set(Tables.STAGES.ID, "stage-1");
            stage.set(Tables.STAGES.NAME, "Stage A");
            stage.set(Tables.STAGES.COMPETITION_ID, "comp-1");
            stage.set(Tables.STAGES.DATE_FROM, 1000L);
            stage.set(Tables.STAGES.DATE_TO, 2000L);
            stage.set(Tables.STAGES.LAST_UPDATE, 10L);
            stage.set(Tables.STAGES.CREATED_AT, 20L);
            stages.add(stage);

            Result<Record> events = mock.newResult(EVENT_FIELDS);
            Record event = mock.newRecord(EVENT_FIELDS);
            event.set(Tables.EVENTS.ID, "event-1");
            event.set(Tables.EVENTS.NAME, "Open");
            event.set(Tables.EVENTS.DISCIPLINE, "OBDX");
            event.set(Tables.EVENTS.STAGE_ID, "stage-1");
            event.set(Tables.EVENTS.LAST_UPDATE, 10L);
            event.set(Tables.EVENTS.CREATED_AT, 20L);
            events.add(event);

            return new MockResult[]{routeHydrator(ctx, competitions, stages, events)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        CompetitionSnapshot competition = new GetCompetitionJooqAdapter(dsl).getCompetition("comp-1");

        assertThat(competition).isNotNull();
        assertThat(competition.id()).isEqualTo("comp-1");
        assertThat(competition.name()).isEqualTo("World Cup");
        assertThat(competition.creator()).isEqualTo("creator-1");
        assertThat(competition.organizerName()).isEqualTo("Acme Org");

        assertThat(competition.stages()).hasSize(1);
        var stage = competition.stages().getFirst();
        assertThat(stage.id()).isEqualTo("stage-1");
        assertThat(stage.competitionId()).isEqualTo("comp-1");

        assertThat(stage.events()).hasSize(1);
        var event = stage.events().getFirst();
        assertThat(event.id()).isEqualTo("event-1");
        assertThat(event.stageId()).isEqualTo("stage-1");
    }

    @Test
    void hydrates_competitor_sex_from_the_dogs_table() {
        MockDataProvider provider = ctx -> {
            DSLContext mock = DSL.using(SQLDialect.POSTGRES);

            if (ctx.sql().toLowerCase().contains("event_competitors")) {
                Result<Record> competitors = mock.newResult(COMPETITOR_FIELDS);
                Record competitor = mock.newRecord(COMPETITOR_FIELDS);
                competitor.set(com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS.EVENT_ID,
                        "event-1");
                competitor.set(com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS.DOG_IDENTIFICATION,
                        "dog-1");
                competitor.set(Tables.DOGS.NAME, "Rex");
                competitor.set(Tables.DOGS.SEX, "MALE");
                competitors.add(competitor);
                return new MockResult[]{new MockResult(competitors.size(), competitors)};
            }

            Result<Record> competitions = mock.newResult(COMPETITION_FIELDS);
            Record comp = mock.newRecord(COMPETITION_FIELDS);
            comp.set(Tables.COMPETITIONS.ID, "comp-1");
            comp.set(Tables.COMPETITIONS.NAME, "World Cup");
            comp.set(Tables.COMPETITIONS.LAST_UPDATE, 10L);
            comp.set(Tables.COMPETITIONS.CREATED_AT, 20L);
            competitions.add(comp);

            Result<Record> stages = mock.newResult(STAGE_FIELDS);
            Record stage = mock.newRecord(STAGE_FIELDS);
            stage.set(Tables.STAGES.ID, "stage-1");
            stage.set(Tables.STAGES.COMPETITION_ID, "comp-1");
            stage.set(Tables.STAGES.DATE_FROM, 1000L);
            stage.set(Tables.STAGES.DATE_TO, 2000L);
            stage.set(Tables.STAGES.LAST_UPDATE, 10L);
            stage.set(Tables.STAGES.CREATED_AT, 20L);
            stages.add(stage);

            Result<Record> events = mock.newResult(EVENT_FIELDS);
            Record event = mock.newRecord(EVENT_FIELDS);
            event.set(Tables.EVENTS.ID, "event-1");
            event.set(Tables.EVENTS.STAGE_ID, "stage-1");
            event.set(Tables.EVENTS.LAST_UPDATE, 10L);
            event.set(Tables.EVENTS.CREATED_AT, 20L);
            events.add(event);

            return new MockResult[]{routeHydrator(ctx, competitions, stages, events)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        CompetitionSnapshot competition = new GetCompetitionJooqAdapter(dsl).getCompetition("comp-1");

        var competitors = competition.stages().getFirst().events().getFirst().competitors();
        assertThat(competitors).hasSize(1);
        assertThat(competitors.getFirst().dogIdentification()).isEqualTo("dog-1");
        assertThat(competitors.getFirst().sex()).isEqualTo(Sex.MALE);
    }

    @Test
    void competition_id_by_stage_returns_id() {
        MockDataProvider provider = _ -> {
            DSLContext mock = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mock.newResult(COMPETITION_ID_FIELDS);
            Record record = mock.newRecord(COMPETITION_ID_FIELDS);
            record.set(Tables.STAGES.COMPETITION_ID, "comp-9");
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        String competitionId = new GetCompetitionJooqAdapter(dsl).competitionIdByStage("stage-9");

        assertThat(competitionId).isEqualTo("comp-9");
    }

    @Test
    void competition_id_by_stage_returns_null_when_absent() {
        MockDataProvider provider = _ -> {
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(COMPETITION_ID_FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        String competitionId = new GetCompetitionJooqAdapter(dsl).competitionIdByStage("missing");

        assertThat(competitionId).isNull();
    }

    @Test
    void competition_id_by_event_returns_id() {
        MockDataProvider provider = _ -> {
            DSLContext mock = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mock.newResult(COMPETITION_ID_FIELDS);
            Record record = mock.newRecord(COMPETITION_ID_FIELDS);
            record.set(Tables.STAGES.COMPETITION_ID, "comp-7");
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        String competitionId = new GetCompetitionJooqAdapter(dsl).competitionIdByEvent("event-7");

        assertThat(competitionId).isEqualTo("comp-7");
    }

    @Test
    void competition_id_by_event_returns_null_when_absent() {
        MockDataProvider provider = _ -> {
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(COMPETITION_ID_FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        String competitionId = new GetCompetitionJooqAdapter(dsl).competitionIdByEvent("missing");

        assertThat(competitionId).isNull();
    }
}
