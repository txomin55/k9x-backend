package com.k9x.domain.competitions.aggregates;

import com.k9x.domain.competitions.commands.CompetitionChange;
import com.k9x.domain.competitions.commands.CompetitionCreated;
import com.k9x.domain.competitions.commands.CompetitionDeleted;
import com.k9x.domain.competitions.commands.CompetitionUpdated;
import com.k9x.domain.competitions.commands.DogEnrolled;
import com.k9x.domain.competitions.commands.EventCreated;
import com.k9x.domain.competitions.commands.EventDeleted;
import com.k9x.domain.competitions.commands.ObdxEventInfoUpdated;
import com.k9x.domain.competitions.commands.ScoreUpdated;
import com.k9x.domain.competitions.commands.StageCreated;
import com.k9x.domain.competitions.commands.StageDeleted;
import com.k9x.domain.competitions.commands.StageRenamed;
import com.k9x.domain.competitions.commands.CompetitionUpdateData;
import com.k9x.domain.competitions.commands.NewEventData;
import com.k9x.domain.competitions.commands.NewStageData;
import com.k9x.domain.competitions.commands.ObdxEventUpdateData;
import com.k9x.domain.competitions.commands.ScoreUpdateData;
import com.k9x.domain.competitions.commands.StageUpdateData;
import com.k9x.domain.competitions.commands.YellowCardData;
import com.k9x.domain.competitions.commands.YellowCardRegistered;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.disciplines.exceptions.DisciplineConfigurationMalformedException;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.valueobjects.EventCompetitor;
import com.k9x.domain.events.valueobjects.Score;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import com.k9x.domain.competitions.exceptions.CompetitionAlreadyDeletedException;
import com.k9x.domain.competitions.exceptions.CompetitionCannotBeDeletedException;
import com.k9x.domain.competitions.exceptions.CompetitionNotFoundException;
import com.k9x.domain.events.exceptions.CompetitorDisqualifiedException;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.domain.events.exceptions.YellowCardAlreadyRegisteredException;
import com.k9x.domain.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.domain.stages.exceptions.StageCannotBeDeletedException;
import com.k9x.domain.stages.exceptions.StageExpiredException;
import com.k9x.domain.stages.exceptions.StageNotStartedException;
import com.k9x.domain.stages.exceptions.StageNotFoundException;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.domain.shared.SupportUser;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompetitionAggregateTest {

    private static final long PAST = Instant.parse("2020-01-01T00:00:00Z").toEpochMilli();
    private static final long NOW = Instant.parse("2024-06-15T12:00:00Z").toEpochMilli();
    private static final long FUTURE = Instant.parse("2030-01-01T00:00:00Z").toEpochMilli();
    private static final String OWNER = "user-1";

    private StageSnapshot activeStage(String creator, Long deletedAt) {
        return new StageSnapshot("stage-1", "Stage 1", "comp-1", creator, FUTURE, FUTURE, 0L, 0L, deletedAt, List.of());
    }

    private CompetitionSnapshot competition(String creator, Long deletedAt, StageSnapshot stage) {
        return new CompetitionSnapshot("comp-1", "World Cup", creator, "Org", "ES", "desc", "addr",
                null, null, 0L, 0L, deletedAt, List.of(stage));
    }

    private CompetitionChange onlyChange(CompetitionAggregate aggregate) {
        assertEquals(1, aggregate.pendingChanges().size());
        return aggregate.pendingChanges().getFirst();
    }

    // ---- of -------------------------------------------------------------------------------------

    @Test
    void of_throws_when_competition_does_not_exist() {
        assertThrows(CompetitionNotFoundException.class, () -> CompetitionAggregate.of(null));
    }

    // ---- createNew / update / delete ------------------------------------------------------------

    @Test
    void createNew_records_competition_created() {
        CompetitionAggregate aggregate = CompetitionAggregate.createNew("comp-1", "Comp", OWNER, NOW);

        CompetitionCreated change = assertInstanceOf(CompetitionCreated.class, onlyChange(aggregate));
        assertEquals("comp-1", change.id());
        assertEquals("Comp", change.name());
        assertEquals(OWNER, change.creator());
        assertEquals(NOW, change.createdAt());
    }

    @Test
    void update_throws_when_competition_is_deleted() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, NOW, activeStage(OWNER, null)));
        assertThrows(CompetitionAlreadyDeletedException.class,
                () -> aggregate.update(new CompetitionUpdateData("N", "D", "ES", "A", 1.0, 2.0), OWNER, NOW));
    }

    @Test
    void update_throws_when_user_is_not_creator() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition("other", null, activeStage("other", null)));
        assertThrows(UnauthorizedResourceException.class,
                () -> aggregate.update(new CompetitionUpdateData("N", "D", "ES", "A", 1.0, 2.0), OWNER, NOW));
    }

    @Test
    void update_records_competition_updated() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage(OWNER, null)));

        aggregate.update(new CompetitionUpdateData("Name", "Desc", "ES", "Addr", 1.5, 2.5), OWNER, NOW);

        CompetitionUpdated change = assertInstanceOf(CompetitionUpdated.class, onlyChange(aggregate));
        assertEquals("comp-1", change.id());
        assertEquals("Name", change.name());
        assertEquals("Addr", change.address());
        assertEquals(1.5, change.coordAlt());
        assertEquals(2.5, change.coordLong());
        assertEquals(NOW, change.lastUpdate());
    }

    @Test
    void delete_throws_when_competition_is_started() {
        EventSnapshot started = new EventSnapshot("evt-1", "cfg-1", "obdx", "Open", "stage-1", OWNER,
                null, 0L, 0L, null, null, List.of(), List.of(), List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", BigDecimal.TEN, 0L)));
        StageSnapshot startedStage = new StageSnapshot("stage-1", "Stage 1", "comp-1", OWNER, FUTURE, FUTURE, 0L, 0L, null,
                List.of(started));
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, startedStage));
        assertThrows(CompetitionCannotBeDeletedException.class, () -> aggregate.delete(OWNER, NOW));
    }

    @Test
    void delete_records_competition_deleted_and_cascades_to_stages_and_events() {
        StageSnapshot stage = stageWith(event(null), FUTURE);
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stage));

        aggregate.delete(OWNER, NOW);

        List<CompetitionChange> changes = aggregate.pendingChanges();
        assertEquals(3, changes.size());
        CompetitionDeleted competitionDeleted = assertInstanceOf(CompetitionDeleted.class, changes.get(0));
        assertEquals("comp-1", competitionDeleted.id());
        assertEquals(NOW, competitionDeleted.deletedAt());
        StageDeleted stageDeleted = assertInstanceOf(StageDeleted.class, changes.get(1));
        assertEquals("stage-1", stageDeleted.id());
        assertEquals(NOW, stageDeleted.deletedAt());
        EventDeleted eventDeleted = assertInstanceOf(EventDeleted.class, changes.get(2));
        assertEquals("evt-1", eventDeleted.id());
        assertEquals(NOW, eventDeleted.deletedAt());
    }

    @Test
    void delete_throws_when_a_stage_has_a_non_created_event() {
        EventSnapshot started = new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", BigDecimal.TEN, 0L)));
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(started, FUTURE)));
        assertThrows(CompetitionCannotBeDeletedException.class, () -> aggregate.delete(OWNER, NOW));
    }

    @Test
    void delete_ignores_already_deleted_stages_when_cascading() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage(OWNER, PAST)));

        aggregate.delete(OWNER, NOW);

        CompetitionDeleted change = assertInstanceOf(CompetitionDeleted.class, onlyChange(aggregate));
        assertEquals("comp-1", change.id());
    }

    // ---- createStage ----------------------------------------------------------------------------

    @Test
    void createStage_throws_when_competition_is_deleted() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, NOW, activeStage(OWNER, null)));
        assertThrows(CompetitionAlreadyDeletedException.class,
                () -> aggregate.createStage(new NewStageData("s", "S", FUTURE, FUTURE), OWNER, NOW));
    }

    @Test
    void createStage_throws_when_user_is_not_competition_creator() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition("other", null, activeStage("other", null)));
        assertThrows(UnauthorizedResourceException.class,
                () -> aggregate.createStage(new NewStageData("s", "S", FUTURE, FUTURE), OWNER, NOW));
    }

    @Test
    void createStage_records_stage_created() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage(OWNER, null)));

        aggregate.createStage(new NewStageData("stage-new", "New", FUTURE, FUTURE), OWNER, NOW);

        StageCreated change = assertInstanceOf(StageCreated.class, onlyChange(aggregate));
        assertEquals("stage-new", change.id());
        assertEquals("comp-1", change.competitionId());
        assertEquals(OWNER, change.creator());
        assertEquals(NOW, change.createdAt());
    }

    // ---- renameStage ----------------------------------------------------------------------------

    @Test
    void renameStage_throws_when_stage_not_found() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage(OWNER, null)));
        assertThrows(StageNotFoundException.class,
                () -> aggregate.renameStage("missing", new StageUpdateData("X", 1L, 2L), OWNER, NOW));
    }

    @Test
    void renameStage_throws_when_stage_is_deleted() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage(OWNER, NOW)));
        assertThrows(StageAlreadyDeletedException.class,
                () -> aggregate.renameStage("stage-1", new StageUpdateData("X", 1L, 2L), OWNER, NOW));
    }

    @Test
    void renameStage_throws_when_user_is_not_stage_creator() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage("other", null)));
        assertThrows(UnauthorizedResourceException.class,
                () -> aggregate.renameStage("stage-1", new StageUpdateData("X", 1L, 2L), OWNER, NOW));
    }

    @Test
    void renameStage_records_stage_renamed() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage(OWNER, null)));

        aggregate.renameStage("stage-1", new StageUpdateData("Renamed", 10L, 20L), OWNER, NOW);

        StageRenamed change = assertInstanceOf(StageRenamed.class, onlyChange(aggregate));
        assertEquals("stage-1", change.id());
        assertEquals("Renamed", change.name());
        assertEquals(10L, change.dateFrom());
        assertEquals(20L, change.dateTo());
        assertEquals(NOW, change.lastUpdate());
    }

    // ---- deleteStage ----------------------------------------------------------------------------

    @Test
    void deleteStage_throws_when_competition_is_deleted() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, NOW, activeStage(OWNER, null)));
        assertThrows(CompetitionAlreadyDeletedException.class,
                () -> aggregate.deleteStage("stage-1", OWNER, NOW));
    }

    @Test
    void deleteStage_throws_when_stage_is_started() {
        EventSnapshot started = new EventSnapshot("evt-1", "cfg-1", "obdx", "Open", "stage-1", OWNER,
                null, 0L, 0L, null, null, List.of(), List.of(), List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", BigDecimal.TEN, 0L)));
        StageSnapshot startedStage = new StageSnapshot("stage-1", "Stage 1", "comp-1", OWNER, FUTURE, FUTURE, 0L, 0L, null,
                List.of(started));
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, startedStage));
        assertThrows(StageCannotBeDeletedException.class, () -> aggregate.deleteStage("stage-1", OWNER, NOW));
    }

    @Test
    void deleteStage_throws_when_stage_is_finished() {
        StageSnapshot finishedStage = new StageSnapshot("stage-1", "Stage 1", "comp-1", OWNER, 0L, 0L, 0L, 0L, null, List.of());
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, finishedStage));
        assertThrows(StageCannotBeDeletedException.class, () -> aggregate.deleteStage("stage-1", OWNER, NOW));
    }

    @Test
    void deleteStage_throws_when_an_event_is_not_created() {
        EventCompetitor settled = new EventCompetitor("dog-1", "Rex", "Owner", "Handler", "Team", "ES", "Breed", null,
                (short) 1, false, true, null);
        EventSnapshot finished = new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(settled), List.of(), List.of(), List.of());
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(finished, FUTURE)));
        assertThrows(StageCannotBeDeletedException.class, () -> aggregate.deleteStage("stage-1", OWNER, NOW));
    }

    @Test
    void deleteStage_records_stage_deleted() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage(OWNER, null)));

        aggregate.deleteStage("stage-1", OWNER, NOW);

        StageDeleted change = assertInstanceOf(StageDeleted.class, onlyChange(aggregate));
        assertEquals("stage-1", change.id());
        assertEquals(NOW, change.deletedAt());
    }

    @Test
    void deleteStage_cascades_soft_delete_to_its_active_events() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(event(null), FUTURE)));

        aggregate.deleteStage("stage-1", OWNER, NOW);

        List<CompetitionChange> changes = aggregate.pendingChanges();
        assertEquals(2, changes.size());
        StageDeleted stageDeleted = assertInstanceOf(StageDeleted.class, changes.get(0));
        assertEquals("stage-1", stageDeleted.id());
        EventDeleted eventDeleted = assertInstanceOf(EventDeleted.class, changes.get(1));
        assertEquals("evt-1", eventDeleted.id());
        assertEquals(NOW, eventDeleted.deletedAt());
    }

    // ---- events ---------------------------------------------------------------------------------

    private EventSnapshot event(Long deletedAt) {
        return new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null, 0L, 0L, deletedAt,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of());
    }

    private StageSnapshot stageWith(EventSnapshot event, long dateTo) {
        return new StageSnapshot("stage-1", "Stage 1", "comp-1", OWNER, PAST, dateTo, 0L, 0L, null, List.of(event));
    }

    @Test
    void createEvent_throws_when_stage_not_found() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage(OWNER, null)));
        assertThrows(StageNotFoundException.class,
                () -> aggregate.createEvent(new NewEventData("e", "E", "missing", "obdx"), OWNER, NOW));
    }

    @Test
    void createEvent_records_event_created() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage(OWNER, null)));

        aggregate.createEvent(new NewEventData("evt-new", "E", "stage-1", "obdx"), OWNER, NOW);

        EventCreated change = assertInstanceOf(EventCreated.class, onlyChange(aggregate));
        assertEquals("evt-new", change.id());
        assertEquals("stage-1", change.stageId());
        assertEquals("OBDX", change.discipline());
        assertEquals(OWNER, change.creator());
    }

    @Test
    void createEvent_throws_when_discipline_is_unknown() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage(OWNER, null)));

        assertThrows(DisciplineConfigurationMalformedException.class,
                () -> aggregate.createEvent(new NewEventData("evt-new", "E", "stage-1", "nope"), OWNER, NOW));
    }

    @Test
    void deleteEvent_throws_when_event_not_found() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage(OWNER, null)));
        assertThrows(EventNotFoundException.class, () -> aggregate.deleteEvent("missing", OWNER, NOW));
    }

    @Test
    void deleteEvent_records_event_deleted() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(event(null), FUTURE)));

        aggregate.deleteEvent("evt-1", OWNER, NOW);

        EventDeleted change = assertInstanceOf(EventDeleted.class, onlyChange(aggregate));
        assertEquals("evt-1", change.id());
        assertEquals(NOW, change.deletedAt());
    }

    @Test
    void enrollDog_throws_when_stage_is_expired() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(event(null), 1L)));
        assertThrows(StageExpiredException.class, () -> aggregate.enrollDog("evt-1", "dog-1", false, OWNER, NOW));
    }

    @Test
    void enrollDog_records_dog_enrolled() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(event(null), FUTURE)));

        aggregate.enrollDog("evt-1", "dog-1", true, OWNER, NOW);

        DogEnrolled change = assertInstanceOf(DogEnrolled.class, onlyChange(aggregate));
        assertEquals("evt-1", change.eventId());
        assertEquals("dog-1", change.dogId());
        assertTrue(change.bih());
        assertEquals((short) 1, change.position());
        assertEquals(NOW, change.lastUpdate());
    }

    @Test
    void enrollDog_assigns_next_position_after_last_enrolled_competitor() {
        EventCompetitor existing = new EventCompetitor("dog-1", "dog-1", "o", "h", "t", "c", "b", "i",
                (short) 3, true, false, null);
        EventSnapshot eventWithCompetitors = new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null,
                0L, 0L, null, ObdxAvgMethod.MID_AVG, List.of(existing), List.of(), List.of(), List.of());
        CompetitionAggregate aggregate =
                CompetitionAggregate.of(competition(OWNER, null, stageWith(eventWithCompetitors, FUTURE)));

        aggregate.enrollDog("evt-1", "dog-2", false, OWNER, NOW);

        DogEnrolled change = assertInstanceOf(DogEnrolled.class, onlyChange(aggregate));
        assertEquals((short) 4, change.position());
    }

    @Test
    void updateObdxEventInfo_throws_when_user_is_not_event_creator() {
        EventSnapshot otherEvent = new EventSnapshot("evt-1", null, null, "Event", "stage-1", "other", null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of());
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(otherEvent, FUTURE)));
        ObdxEventUpdateData data = new ObdxEventUpdateData("E", "cfg", ObdxAvgMethod.MID_AVG, null,
                List.of(), List.of(), List.of());
        assertThrows(UnauthorizedResourceException.class,
                () -> aggregate.updateObdxEventInfo("evt-1", data, OWNER, NOW));
    }

    @Test
    void updateObdxEventInfo_records_obdx_event_info_updated() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(event(null), FUTURE)));
        ObdxEventUpdateData data = new ObdxEventUpdateData("Event", "cfg-1", ObdxAvgMethod.MID_AVG, 100L,
                List.of(), List.of(), List.of());

        aggregate.updateObdxEventInfo("evt-1", data, OWNER, NOW);

        ObdxEventInfoUpdated change = assertInstanceOf(ObdxEventInfoUpdated.class, onlyChange(aggregate));
        assertEquals("evt-1", change.eventId());
        assertEquals("cfg-1", change.configurationId());
        assertEquals(NOW, change.lastUpdate());
    }

    @Test
    void updateScore_throws_when_stage_has_not_started() {
        StageSnapshot notStarted = new StageSnapshot("stage-1", "Stage 1", "comp-1", OWNER, FUTURE, FUTURE, 0L, 0L, null,
                List.of(event(null)));
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, notStarted));
        ScoreUpdateData data = new ScoreUpdateData("judge-1", "ex-1", "dog-1", BigDecimal.TEN);
        assertThrows(StageNotStartedException.class, () -> aggregate.updateScore("evt-1", data, OWNER, NOW));
    }

    @Test
    void updateScore_throws_when_stage_is_expired() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(event(null), 1L)));
        ScoreUpdateData data = new ScoreUpdateData("judge-1", "ex-1", "dog-1", BigDecimal.TEN);
        assertThrows(StageExpiredException.class, () -> aggregate.updateScore("evt-1", data, OWNER, NOW));
    }

    @Test
    void updateScore_records_score_updated() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(event(null), FUTURE)));
        ScoreUpdateData data = new ScoreUpdateData("judge-1", "ex-1", "dog-1", BigDecimal.TEN);

        aggregate.updateScore("evt-1", data, OWNER, NOW);

        ScoreUpdated change = assertInstanceOf(ScoreUpdated.class, onlyChange(aggregate));
        assertEquals("evt-1", change.eventId());
        assertEquals("judge-1", change.judgeId());
        assertEquals("dog-1", change.dogId());
        assertEquals(BigDecimal.TEN, change.score());
    }

    @Test
    void updateScore_throws_when_competitor_is_disqualified() {
        EventSnapshot disqualified = new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", null, 0L, 1000L),
                        new Score("ex-2", "judge-1", "dog-1", null, 0L, 2000L)));
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(disqualified, FUTURE)));
        ScoreUpdateData data = new ScoreUpdateData("judge-1", "ex-3", "dog-1", BigDecimal.TEN);

        assertThrows(CompetitorDisqualifiedException.class, () -> aggregate.updateScore("evt-1", data, OWNER, NOW));
    }

    @Test
    void registerYellowCard_records_yellow_card_registered() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(event(null), FUTURE)));
        YellowCardData data = new YellowCardData("judge-1", "ex-1", "dog-1");

        aggregate.registerYellowCard("evt-1", data, OWNER, NOW);

        YellowCardRegistered change = assertInstanceOf(YellowCardRegistered.class, onlyChange(aggregate));
        assertEquals("evt-1", change.eventId());
        assertEquals("judge-1", change.judgeId());
        assertEquals("ex-1", change.exerciseId());
        assertEquals("dog-1", change.dogId());
    }

    @Test
    void registerYellowCard_throws_when_already_registered_for_judge_exercise_and_dog() {
        EventSnapshot carded = new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", null, 0L, 1000L)));
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(carded, FUTURE)));
        YellowCardData data = new YellowCardData("judge-1", "ex-1", "dog-1");

        assertThrows(YellowCardAlreadyRegisteredException.class,
                () -> aggregate.registerYellowCard("evt-1", data, OWNER, NOW));
    }

    // ---- support superuser bypass ----------------------------------------------------------------

    private static final String SUPPORT = SupportUser.EMAIL;

    @Test
    void support_can_update_a_deleted_competition_it_does_not_own() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition("other", NOW, activeStage("other", null)));

        aggregate.update(new CompetitionUpdateData("Name", "Desc", "ES", "Addr", 1.5, 2.5), SUPPORT, NOW);

        assertInstanceOf(CompetitionUpdated.class, onlyChange(aggregate));
    }

    @Test
    void support_can_delete_a_started_competition_it_does_not_own() {
        EventSnapshot started = new EventSnapshot("evt-1", "cfg-1", "obdx", "Open", "stage-1", "other",
                null, 0L, 0L, null, null, List.of(), List.of(), List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", BigDecimal.TEN, 0L)));
        StageSnapshot startedStage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "other", FUTURE, FUTURE, 0L, 0L, null,
                List.of(started));
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition("other", null, startedStage));

        aggregate.delete(SUPPORT, NOW);

        assertInstanceOf(CompetitionDeleted.class, aggregate.pendingChanges().getFirst());
    }

    @Test
    void support_can_delete_an_already_deleted_event_it_does_not_own() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition("other", null, stageWith(event(NOW), FUTURE)));

        aggregate.deleteEvent("evt-1", SUPPORT, NOW);

        EventDeleted change = assertInstanceOf(EventDeleted.class, onlyChange(aggregate));
        assertEquals("evt-1", change.id());
    }

    @Test
    void support_can_enroll_on_an_expired_stage() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition("other", null, stageWith(event(null), 1L)));

        aggregate.enrollDog("evt-1", "dog-1", false, SUPPORT, NOW);

        assertInstanceOf(DogEnrolled.class, onlyChange(aggregate));
    }

    @Test
    void support_can_update_score_on_an_expired_stage() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition("other", null, stageWith(event(null), 1L)));
        ScoreUpdateData data = new ScoreUpdateData("judge-1", "ex-1", "dog-1", BigDecimal.TEN);

        aggregate.updateScore("evt-1", data, SUPPORT, NOW);

        assertInstanceOf(ScoreUpdated.class, onlyChange(aggregate));
    }

    @Test
    void support_can_update_obdx_event_info_it_does_not_own() {
        EventSnapshot otherEvent = new EventSnapshot("evt-1", null, null, "Event", "stage-1", "other", null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of());
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition("other", null, stageWith(otherEvent, FUTURE)));
        ObdxEventUpdateData data = new ObdxEventUpdateData("E", "cfg", ObdxAvgMethod.MID_AVG, null,
                List.of(), List.of(), List.of());

        aggregate.updateObdxEventInfo("evt-1", data, SUPPORT, NOW);

        assertInstanceOf(ObdxEventInfoUpdated.class, onlyChange(aggregate));
    }

    @Test
    void support_still_gets_not_found_for_a_missing_event() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition("other", null, stageWith(event(null), FUTURE)));

        assertThrows(EventNotFoundException.class, () -> aggregate.deleteEvent("missing", SUPPORT, NOW));
    }
}
