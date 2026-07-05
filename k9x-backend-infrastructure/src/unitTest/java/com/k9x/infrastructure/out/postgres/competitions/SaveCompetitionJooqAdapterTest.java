package com.k9x.infrastructure.out.postgres.competitions;

import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.competitions.commands.CompetitionUpdateData;
import com.k9x.domain.competitions.commands.NewEventData;
import com.k9x.domain.competitions.commands.NewStageData;
import com.k9x.domain.competitions.commands.ObdxCompetitorItem;
import com.k9x.domain.competitions.commands.ObdxEventUpdateData;
import com.k9x.domain.competitions.commands.ObdxExerciseItem;
import com.k9x.domain.competitions.commands.ObdxJudgeItem;
import com.k9x.domain.competitions.commands.ScoreUpdateData;
import com.k9x.domain.competitions.commands.StageUpdateData;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.valueobjects.EventCompetitor;
import com.k9x.domain.stages.aggregates.StageSnapshot;

import java.math.BigDecimal;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
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

class SaveCompetitionJooqAdapterTest {

    private static final long NOW = 1700000000000L;
    private static final long FUTURE_FROM = 1900000000000L;
    private static final long FUTURE_TO = 1900000086400000L;
    private static final long PAST_FROM = 1600000000000L;

    private List<String> capturedSql;
    private DSLContext dsl;

    private void givenCapturingDsl() {
        capturedSql = new ArrayList<>();
        MockDataProvider provider = ctx -> {
            capturedSql.add(ctx.sql());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.STAGES.fields());
            return new MockResult[]{new MockResult(1, result)};
        };
        dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
    }

    private CompetitionAggregate aggregateWithActiveStage() {
        StageSnapshot stage = new StageSnapshot("stage-123", "Stage", "comp-1", "user",
                FUTURE_FROM, FUTURE_TO, NOW, NOW, null, List.of());
        CompetitionSnapshot competition = new CompetitionSnapshot("comp-1", "Comp", "user", "Org", "ES", "desc", "addr",
                0.0, 0.0, NOW, NOW, null, List.of(stage));
        return CompetitionAggregate.of(competition);
    }

    private CompetitionAggregate aggregateWithActiveEvent() {
        EventSnapshot event = new EventSnapshot("evt-1", null, null, "Event", "stage-123", "user", FUTURE_TO, NOW, NOW, null,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), List.of());
        StageSnapshot stage = new StageSnapshot("stage-123", "Stage", "comp-1", "user",
                FUTURE_FROM, FUTURE_TO, NOW, NOW, null, List.of(event));
        CompetitionSnapshot competition = new CompetitionSnapshot("comp-1", "Comp", "user", "Org", "ES", "desc", "addr",
                0.0, 0.0, NOW, NOW, null, List.of(stage));
        return CompetitionAggregate.of(competition);
    }

    // Stage window [PAST_FROM, FUTURE_TO] contains NOW, so the stage is already started — required for scoring.
    private CompetitionAggregate aggregateWithStartedEvent() {
        EventSnapshot event = new EventSnapshot("evt-1", null, null, "Event", "stage-123", "user", null, NOW, NOW, null,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), List.of());
        StageSnapshot stage = new StageSnapshot("stage-123", "Stage", "comp-1", "user",
                PAST_FROM, FUTURE_TO, NOW, NOW, null, List.of(event));
        CompetitionSnapshot competition = new CompetitionSnapshot("comp-1", "Comp", "user", "Org", "ES", "desc", "addr",
                0.0, 0.0, NOW, NOW, null, List.of(stage));
        return CompetitionAggregate.of(competition);
    }

    @Test
    void emits_insert_for_competition_created() {
        givenCapturingDsl();
        CompetitionAggregate competition = CompetitionAggregate.createNew("comp-1", "Comp", "user", NOW);

        new SaveCompetitionJooqAdapter(dsl).save(competition);

        assertThat(capturedSql).anyMatch(sql -> sql.contains("insert into \"k9x\".\"competitions\""));
    }

    @Test
    void emits_update_for_competition_updated() {
        givenCapturingDsl();
        CompetitionAggregate competition = aggregateWithActiveStage();
        competition.update(new CompetitionUpdateData("Comp", "desc", "ES", "addr", 1.0, 2.0), "user", NOW);

        new SaveCompetitionJooqAdapter(dsl).save(competition);

        assertThat(capturedSql).anyMatch(sql ->
                sql.contains("update \"k9x\".\"competitions\"") && sql.contains("\"address\""));
    }

    @Test
    void emits_soft_delete_for_competition_deleted() {
        givenCapturingDsl();
        CompetitionAggregate competition = aggregateWithActiveStage();
        competition.delete("user", NOW);

        new SaveCompetitionJooqAdapter(dsl).save(competition);

        assertThat(capturedSql).anyMatch(sql ->
                sql.contains("update \"k9x\".\"competitions\"") && sql.contains("\"deleted_at\""));
    }

    @Test
    void emits_insert_for_stage_created() {
        givenCapturingDsl();
        CompetitionAggregate competition = aggregateWithActiveStage();
        competition.createStage(new NewStageData("stage-new", "New", FUTURE_FROM, FUTURE_TO), "user", NOW);

        new SaveCompetitionJooqAdapter(dsl).save(competition);

        assertThat(capturedSql).anyMatch(sql -> sql.contains("insert into \"k9x\".\"stages\""));
    }

    @Test
    void emits_update_for_stage_renamed() {
        givenCapturingDsl();
        CompetitionAggregate competition = aggregateWithActiveStage();
        competition.renameStage("stage-123", new StageUpdateData("Renamed", 1L, 2L), "user", NOW);

        new SaveCompetitionJooqAdapter(dsl).save(competition);

        assertThat(capturedSql).anyMatch(sql ->
                sql.contains("update \"k9x\".\"stages\"") && sql.contains("\"name\""));
    }

    @Test
    void emits_soft_delete_for_stage_deleted() {
        givenCapturingDsl();
        CompetitionAggregate competition = aggregateWithActiveStage();
        competition.deleteStage("stage-123", "user", NOW);

        new SaveCompetitionJooqAdapter(dsl).save(competition);

        assertThat(capturedSql).anyMatch(sql ->
                sql.contains("update \"k9x\".\"stages\"") && sql.contains("\"deleted_at\""));
    }

    @Test
    void emits_soft_delete_for_stage_and_its_events_when_stage_deleted() {
        givenCapturingDsl();
        CompetitionAggregate competition = aggregateWithActiveEvent();
        competition.deleteStage("stage-123", "user", NOW);

        new SaveCompetitionJooqAdapter(dsl).save(competition);

        assertThat(capturedSql).anyMatch(sql ->
                sql.contains("update \"k9x\".\"stages\"") && sql.contains("\"deleted_at\""));
        assertThat(capturedSql).anyMatch(sql ->
                sql.contains("update \"k9x\".\"events\"") && sql.contains("\"deleted_at\""));
    }

    @Test
    void emits_insert_for_event_created() {
        givenCapturingDsl();
        CompetitionAggregate competition = aggregateWithActiveStage();
        competition.createEvent(new NewEventData("evt-new", "E", "stage-123", "obdx"), "user", NOW);

        new SaveCompetitionJooqAdapter(dsl).save(competition);

        assertThat(capturedSql).anyMatch(sql -> sql.contains("insert into \"k9x\".\"events\""));
    }

    @Test
    void emits_soft_delete_for_event_deleted() {
        givenCapturingDsl();
        CompetitionAggregate competition = aggregateWithActiveEvent();
        competition.deleteEvent("evt-1", "user", NOW);

        new SaveCompetitionJooqAdapter(dsl).save(competition);

        assertThat(capturedSql).anyMatch(sql ->
                sql.contains("update \"k9x\".\"events\"") && sql.contains("\"deleted_at\""));
    }

    @Test
    void emits_insert_for_dog_enrolled() {
        givenCapturingDsl();
        CompetitionAggregate competition = aggregateWithActiveEvent();
        competition.enrollDog("evt-1", "dog-1", false, "user-1", NOW);

        new SaveCompetitionJooqAdapter(dsl).save(competition);

        assertThat(capturedSql).anyMatch(sql -> sql.contains("insert into \"obdx\".\"event_competitors\""));
    }

    @Test
    void emits_full_statement_sequence_for_obdx_event_info_updated() {
        givenCapturingDsl();
        CompetitionAggregate competition = aggregateWithActiveEvent();
        ObdxEventUpdateData data = new ObdxEventUpdateData("Event", "config-1", ObdxAvgMethod.MID_AVG, 1735689600000L,
                List.of(new ObdxCompetitorItem("dog-1", (short) 1, true)),
                List.of(new ObdxExerciseItem("exercise-1", (short) 1, new String[]{"tag1"})),
                List.of(new ObdxJudgeItem("judge-1", "collector@example.com")), List.of());
        competition.updateObdxEventInfo("evt-1", data, "user", NOW);

        new SaveCompetitionJooqAdapter(dsl).save(competition);

        assertThat(capturedSql).hasSize(9);
        assertThat(capturedSql.get(0)).contains("update \"k9x\".\"events\"");
        assertThat(capturedSql.get(1)).contains("delete from \"obdx\".\"event_scores\"");
        assertThat(capturedSql.get(2)).contains("delete from \"obdx\".\"event_competitors\"");
        assertThat(capturedSql.get(3)).contains("insert into \"obdx\".\"event_competitors\"");
        assertThat(capturedSql.get(4)).contains("delete from \"obdx\".\"event_exercises\"");
        assertThat(capturedSql.get(5)).contains("insert into \"obdx\".\"event_exercises\"");
        assertThat(capturedSql.get(6)).contains("delete from \"obdx\".\"event_judges\"");
        assertThat(capturedSql.get(7)).contains("insert into \"obdx\".\"event_judges\"");
        assertThat(capturedSql.get(8))
                .contains("insert into \"obdx\".\"event_scores\"")
                .contains("on conflict")
                .contains("do nothing");
    }

    @Test
    void emits_update_for_competitor_not_competing() {
        givenCapturingDsl();
        EventCompetitor competitor = new EventCompetitor("dog-1", "Rex", "Owner", "Handler", "Team", "ES", "Breed", null,
                (short) 1, true, false, null, null, null);
        EventSnapshot event = new EventSnapshot("evt-1", null, null, "Event", "stage-123", "user", null, NOW, NOW, null,
                ObdxAvgMethod.MID_AVG, List.of(competitor), List.of(), List.of(), List.of(), List.of());
        StageSnapshot stage = new StageSnapshot("stage-123", "Stage", "comp-1", "user",
                FUTURE_FROM, FUTURE_TO, NOW, NOW, null, List.of(event));
        CompetitionSnapshot competition = new CompetitionSnapshot("comp-1", "Comp", "user", "Org", "ES", "desc", "addr",
                0.0, 0.0, NOW, NOW, null, List.of(stage));
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition);
        aggregate.updateCompetitorNotCompeting("evt-1", "dog-1", true, "user", NOW);

        new SaveCompetitionJooqAdapter(dsl).save(aggregate);

        assertThat(capturedSql).anyMatch(sql ->
                sql.contains("update \"obdx\".\"event_competitors\"") && sql.contains("\"not_competing\""));
    }

    @Test
    void emits_upsert_for_score_updated() {
        givenCapturingDsl();
        CompetitionAggregate competition = aggregateWithStartedEvent();
        competition.updateScore("evt-1",
                new ScoreUpdateData("judge-1", "exercise-1", "dog-1", BigDecimal.TEN), "user-1", NOW);

        new SaveCompetitionJooqAdapter(dsl).save(competition);

        assertThat(capturedSql).anyMatch(sql ->
                sql.contains("insert into \"obdx\".\"event_scores\"")
                        && sql.contains("on conflict") && sql.contains("do update"));
    }

    @Test
    void emits_one_statement_per_change_for_multi_change_aggregate() {
        givenCapturingDsl();
        CompetitionAggregate competition = aggregateWithActiveStage();
        competition.createStage(new NewStageData("stage-new", "New", FUTURE_FROM, FUTURE_TO), "user", NOW);
        competition.renameStage("stage-123", new StageUpdateData("Renamed", 1L, 2L), "user", NOW);
        competition.deleteStage("stage-123", "user", NOW);

        new SaveCompetitionJooqAdapter(dsl).save(competition);

        assertThat(capturedSql).hasSize(3);
    }
}
