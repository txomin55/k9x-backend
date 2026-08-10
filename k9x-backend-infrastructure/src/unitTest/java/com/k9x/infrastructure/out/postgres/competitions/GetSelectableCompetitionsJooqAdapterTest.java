package com.k9x.infrastructure.out.postgres.competitions;

import com.k9x.application.competitions.use_case.dto.FetchSelectableCompetitionDTO;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GetSelectableCompetitionsJooqAdapterTest {

    private static final Field<?>[] FIELDS = {
            Tables.COMPETITIONS.ID,
            Tables.COMPETITIONS.NAME,
            Tables.STAGES.ID,
            Tables.STAGES.NAME,
            Tables.EVENTS.ID,
            Tables.EVENTS.NAME
    };

    private static Record row(DSLContext dsl, String competitionId, String competitionName,
                              String stageId, String stageName, String eventId, String eventName) {
        Record record = dsl.newRecord(FIELDS);
        record.set(Tables.COMPETITIONS.ID, competitionId);
        record.set(Tables.COMPETITIONS.NAME, competitionName);
        record.set(Tables.STAGES.ID, stageId);
        record.set(Tables.STAGES.NAME, stageName);
        record.set(Tables.EVENTS.ID, eventId);
        record.set(Tables.EVENTS.NAME, eventName);
        return record;
    }

    private static MockDataProvider returning(Record... records) {
        return ignored -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(FIELDS);
            result.addAll(List.of(records));
            return new MockResult[]{new MockResult(records.length, result)};
        };
    }

    @Test
    void skips_deleted_competitions_trials_and_events() {
        AtomicReference<String> capturedSql = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetSelectableCompetitionsJooqAdapter(dsl).getSelectableCompetitions();

        // The joined filters sit in the ON clauses so an empty branch is still returned; only the competition
        // filter belongs to the WHERE.
        assertThat(capturedSql.get())
                .contains("from \"k9x\".\"competitions\"")
                .contains("left outer join \"k9x\".\"stages\"")
                .contains("left outer join \"k9x\".\"events\"")
                .containsPattern("where .*\"competitions\"\\.\"deleted_at\" is null");
    }

    @Test
    void folds_the_flat_rows_into_a_competition_trial_event_tree() {
        DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
        DSLContext dsl = DSL.using(new MockConnection(returning(
                row(mockDsl, "comp-1", "Copa", "stage-1", "Trial 1", "event-1", "Event 1"),
                row(mockDsl, "comp-1", "Copa", "stage-1", "Trial 1", "event-2", "Event 2"),
                row(mockDsl, "comp-1", "Copa", "stage-2", "Trial 2", "event-3", "Event 3"),
                row(mockDsl, "comp-2", "Liga", "stage-3", "Trial 3", "event-4", "Event 4"))),
                SQLDialect.POSTGRES);

        List<FetchSelectableCompetitionDTO> competitions =
                new GetSelectableCompetitionsJooqAdapter(dsl).getSelectableCompetitions();

        assertThat(competitions).extracting(FetchSelectableCompetitionDTO::id)
                .containsExactly("comp-1", "comp-2");
        assertThat(competitions.getFirst().stages()).hasSize(2);
        assertThat(competitions.getFirst().stages().getFirst().events())
                .extracting(event -> event.id()).containsExactly("event-1", "event-2");
        assertThat(competitions.get(1).stages().getFirst().events()).hasSize(1);
    }

    @Test
    void keeps_a_competition_with_no_trials() {
        DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
        DSLContext dsl = DSL.using(new MockConnection(returning(
                row(mockDsl, "comp-1", "Copa", null, null, null, null))), SQLDialect.POSTGRES);

        List<FetchSelectableCompetitionDTO> competitions =
                new GetSelectableCompetitionsJooqAdapter(dsl).getSelectableCompetitions();

        assertThat(competitions).hasSize(1);
        assertThat(competitions.getFirst().stages()).isEmpty();
    }

    @Test
    void keeps_a_trial_with_no_events() {
        DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
        DSLContext dsl = DSL.using(new MockConnection(returning(
                row(mockDsl, "comp-1", "Copa", "stage-1", "Trial 1", null, null))), SQLDialect.POSTGRES);

        List<FetchSelectableCompetitionDTO> competitions =
                new GetSelectableCompetitionsJooqAdapter(dsl).getSelectableCompetitions();

        assertThat(competitions.getFirst().stages()).hasSize(1);
        assertThat(competitions.getFirst().stages().getFirst().events()).isEmpty();
    }
}
