package com.k9x.infrastructure.out.postgres.competitions;

import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code getCompetitions(creator)} hydrates full {@link CompetitionSnapshot} trees through
 * {@link CompetitionHydrator}, so the {@link MockDataProvider} routes by FROM table, mirroring
 * {@link GetCompetitionJooqAdapterTest}.
 */
class GetCompetitionListJooqAdapterTest {

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
            Tables.EVENTS.CONFIGURATION_ID,
            Tables.EVENTS.DISCIPLINE,
            Tables.EVENTS.NAME,
            Tables.EVENTS.STAGE_ID,
            Tables.EVENTS.CREATOR,
            Tables.EVENTS.ENROLLMENT_DEADLINE,
            Tables.EVENTS.LAST_UPDATE,
            Tables.EVENTS.CREATED_AT,
            Tables.EVENTS.DELETED_AT,
            Tables.EVENTS.SCORE_CALCULATION,
            Tables.EVENTS.AWARDS,
            Tables.EVENTS.RANK,
            Tables.EVENTS.RANK_SCORE
    };

    private static MockResult emptyNoFields() {
        return new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult());
    }

    private static MockResult routeHydrator(MockExecuteContext ctx, Result<Record> competitions,
                                            Result<Record> stages, Result<Record> events) {
        String sql = ctx.sql().toLowerCase();
        if (sql.contains("event_competitors") || sql.contains("event_exercises")
                || sql.contains("event_judges") || sql.contains("event_scores")) {
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
    void returns_empty_list_when_creator_has_no_competitions() {
        MockDataProvider provider = ctx -> {
            DSLContext mock = DSL.using(SQLDialect.POSTGRES);
            return new MockResult[]{routeHydrator(ctx,
                    mock.newResult(COMPETITION_FIELDS),
                    mock.newResult(STAGE_FIELDS),
                    mock.newResult(EVENT_FIELDS))};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<CompetitionSnapshot> competitions = new GetCompetitionListJooqAdapter(dsl).getCompetitions("creator-1");

        assertThat(competitions).isEmpty();
    }

    @Test
    void returns_competition_trees_for_creator() {
        MockDataProvider provider = ctx -> {
            DSLContext mock = DSL.using(SQLDialect.POSTGRES);

            Result<Record> competitions = mock.newResult(COMPETITION_FIELDS);
            Record comp = mock.newRecord(COMPETITION_FIELDS);
            comp.set(Tables.COMPETITIONS.ID, "comp-1");
            comp.set(Tables.COMPETITIONS.NAME, "World Cup");
            comp.set(Tables.COMPETITIONS.CREATOR, "creator-1");
            comp.set(ORGANIZER_NAME, "Acme Org");
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
        List<CompetitionSnapshot> competitions = new GetCompetitionListJooqAdapter(dsl).getCompetitions("creator-1");

        assertThat(competitions).hasSize(1);
        CompetitionSnapshot competition = competitions.getFirst();
        assertThat(competition.id()).isEqualTo("comp-1");
        assertThat(competition.name()).isEqualTo("World Cup");
        assertThat(competition.creator()).isEqualTo("creator-1");
        assertThat(competition.organizerName()).isEqualTo("Acme Org");

        assertThat(competition.stages()).hasSize(1);
        var stage = competition.stages().getFirst();
        assertThat(stage.id()).isEqualTo("stage-1");
        assertThat(stage.events()).hasSize(1);
        assertThat(stage.events().getFirst().id()).isEqualTo("event-1");
        assertThat(stage.events().getFirst().stageId()).isEqualTo("stage-1");
    }
}
