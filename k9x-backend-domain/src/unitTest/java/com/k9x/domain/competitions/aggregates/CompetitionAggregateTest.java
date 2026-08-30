package com.k9x.domain.competitions.aggregates;

import com.k9x.domain.competitions.aggregates.CompetitionSource;

import com.k9x.domain.competitions.commands.*;
import com.k9x.domain.competitions.exceptions.CompetitionAlreadyDeletedException;
import com.k9x.domain.competitions.exceptions.CompetitionCannotBeDeletedException;
import com.k9x.domain.competitions.exceptions.CompetitionCannotBeUpdatedException;
import com.k9x.domain.competitions.exceptions.CompetitionNotFoundException;
import com.k9x.domain.disciplines.exceptions.DisciplineConfigurationMalformedException;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.disciplines.obdx.exceptions.ObdxMultipleMainJudgesException;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.exceptions.*;
import com.k9x.domain.events.valueobjects.CompetitorDogSnapshot;
import com.k9x.domain.events.valueobjects.EventCompetitor;
import com.k9x.domain.events.valueobjects.EventExercise;
import com.k9x.domain.events.valueobjects.Score;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import com.k9x.domain.stages.exceptions.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompetitionAggregateTest {

    private static final long PAST = Instant.parse("2020-01-01T00:00:00Z").toEpochMilli();
    private static final long NOW = Instant.parse("2024-06-15T12:00:00Z").toEpochMilli();
    private static final long FUTURE = Instant.parse("2030-01-01T00:00:00Z").toEpochMilli();
    private static final String OWNER = "user-1";
    private static final String SMOKE_CREATOR = "k9x.support@gmail.com";
    private static final CompetitorDogSnapshot SNAPSHOT = new CompetitorDogSnapshot("handler", "ES", "team");

    private StageSnapshot activeStage(String creator, Long deletedAt) {
        return new StageSnapshot("stage-1", "Stage 1", "comp-1", creator, FUTURE, FUTURE, 0L, 0L, deletedAt, List.of());
    }

    private CompetitionSnapshot competition(String creator, Long deletedAt, StageSnapshot stage) {
        return new CompetitionSnapshot("comp-1", "World Cup", creator, "Org", "ES", "desc", "addr",
                null, null, CompetitionSource.API, null, 0L, 0L, deletedAt, List.of(stage));
    }

    // ---- of -------------------------------------------------------------------------------------

    private CompetitionChange onlyChange(CompetitionAggregate aggregate) {
        assertEquals(1, aggregate.pendingChanges().size());
        return aggregate.pendingChanges().getFirst();
    }

    // ---- createNew / update / delete ------------------------------------------------------------

    @Test
    void of_throws_when_competition_does_not_exist() {
        assertThrows(CompetitionNotFoundException.class, () -> CompetitionAggregate.of(null));
    }

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
    void update_throws_when_competition_is_finished() {
        StageSnapshot finishedStage = new StageSnapshot("stage-1", "Stage 1", "comp-1", OWNER, 0L, 0L, 0L, 0L, null, List.of());
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, finishedStage));
        assertThrows(CompetitionCannotBeUpdatedException.class,
                () -> aggregate.update(new CompetitionUpdateData("N", "D", "ES", "A", 1.0, 2.0), OWNER, NOW));
    }

    @Test
    void update_throws_when_competition_has_started() {
        // A stage whose date range has begun -> stage TO_START -> competition STARTED, no longer editable.
        StageSnapshot startedStage = new StageSnapshot("stage-1", "Stage 1", "comp-1", OWNER, PAST, FUTURE, 0L, 0L, null, List.of());
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, startedStage));
        assertThrows(CompetitionCannotBeUpdatedException.class,
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
                List.of(new Score("ex-1", "judge-1", "dog-1", BigDecimal.TEN, 0L)), List.of(), null, null, null);
        StageSnapshot startedStage = new StageSnapshot("stage-1", "Stage 1", "comp-1", OWNER, FUTURE, FUTURE, 0L, 0L, null,
                List.of(started));
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, startedStage));
        assertThrows(CompetitionCannotBeDeletedException.class, () -> aggregate.delete(OWNER, NOW));
    }

    @Test
    void delete_records_competition_deleted_and_cascades_to_stages_and_events() {
        // dateFrom in the future -> stage is CREATED (not yet TO_START), so the competition is deletable.
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", OWNER, FUTURE, FUTURE, 0L, 0L, null,
                List.of(event(null)));
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stage));

        aggregate.delete(OWNER, NOW);

        List<CompetitionChange> changes = aggregate.pendingChanges();
        assertEquals(3, changes.size());
        CompetitionDeleted competitionDeleted = assertInstanceOf(CompetitionDeleted.class, changes.getFirst());
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
    void delete_allows_started_smoke_competition_owned_by_support_account() {
        EventSnapshot started = new EventSnapshot("evt-1", "cfg-1", "obdx", "Open", "stage-1", SMOKE_CREATOR,
                null, 0L, 0L, null, null, List.of(), List.of(), List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", BigDecimal.TEN, 0L)), List.of(), null, null, null);
        StageSnapshot startedStage = new StageSnapshot("stage-1", "Stage 1", "comp-1", SMOKE_CREATOR, FUTURE, FUTURE,
                0L, 0L, null, List.of(started));
        CompetitionSnapshot smoke = new CompetitionSnapshot("comp-1", "--SMOKE-- Competition 1 (0803-120000)",
                SMOKE_CREATOR, "Org", "ES", "desc", "addr", null, null, CompetitionSource.API, null, 0L, 0L, null, List.of(startedStage));
        CompetitionAggregate aggregate = CompetitionAggregate.of(smoke);

        aggregate.delete(SMOKE_CREATOR, NOW);

        assertEquals(3, aggregate.pendingChanges().size());
        assertInstanceOf(CompetitionDeleted.class, aggregate.pendingChanges().getFirst());
    }

    @Test
    void delete_throws_when_smoke_prefixed_competition_is_not_owned_by_support_account() {
        EventSnapshot started = new EventSnapshot("evt-1", "cfg-1", "obdx", "Open", "stage-1", OWNER,
                null, 0L, 0L, null, null, List.of(), List.of(), List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", BigDecimal.TEN, 0L)), List.of(), null, null, null);
        StageSnapshot startedStage = new StageSnapshot("stage-1", "Stage 1", "comp-1", OWNER, FUTURE, FUTURE,
                0L, 0L, null, List.of(started));
        CompetitionSnapshot prefixedOnly = new CompetitionSnapshot("comp-1", "--SMOKE-- Competition 1 (0803-120000)",
                OWNER, "Org", "ES", "desc", "addr", null, null, CompetitionSource.API, null, 0L, 0L, null, List.of(startedStage));
        CompetitionAggregate aggregate = CompetitionAggregate.of(prefixedOnly);

        assertThrows(CompetitionCannotBeDeletedException.class, () -> aggregate.delete(OWNER, NOW));
    }

    @Test
    void delete_throws_when_support_account_competition_has_no_smoke_prefix() {
        EventSnapshot started = new EventSnapshot("evt-1", "cfg-1", "obdx", "Open", "stage-1", SMOKE_CREATOR,
                null, 0L, 0L, null, null, List.of(), List.of(), List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", BigDecimal.TEN, 0L)), List.of(), null, null, null);
        StageSnapshot startedStage = new StageSnapshot("stage-1", "Stage 1", "comp-1", SMOKE_CREATOR, FUTURE, FUTURE,
                0L, 0L, null, List.of(started));
        CompetitionSnapshot unprefixed = new CompetitionSnapshot("comp-1", "World Cup",
                SMOKE_CREATOR, "Org", "ES", "desc", "addr", null, null, CompetitionSource.API, null, 0L, 0L, null, List.of(startedStage));
        CompetitionAggregate aggregate = CompetitionAggregate.of(unprefixed);

        assertThrows(CompetitionCannotBeDeletedException.class, () -> aggregate.delete(SMOKE_CREATOR, NOW));
    }

    @Test
    void delete_throws_when_a_stage_has_a_non_created_event() {
        EventSnapshot started = new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", BigDecimal.TEN, 0L)), List.of(), null, null, null);
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(started, FUTURE)));
        assertThrows(CompetitionCannotBeDeletedException.class, () -> aggregate.delete(OWNER, NOW));
    }

    // ---- createStage ----------------------------------------------------------------------------

    @Test
    void delete_ignores_already_deleted_stages_when_cascading() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage(OWNER, PAST)));

        aggregate.delete(OWNER, NOW);

        CompetitionDeleted change = assertInstanceOf(CompetitionDeleted.class, onlyChange(aggregate));
        assertEquals("comp-1", change.id());
    }

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
    void createStage_throws_when_date_to_is_before_date_from() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage(OWNER, null)));
        assertThrows(StageDateToBeforeDateFromException.class,
                () -> aggregate.createStage(new NewStageData("s", "S", NOW, PAST), OWNER, NOW));
    }

    @Test
    void createStage_allows_date_to_on_the_same_day_as_date_from() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage(OWNER, null)));

        aggregate.createStage(new NewStageData("s", "S", NOW, NOW), OWNER, NOW);

        assertInstanceOf(StageCreated.class, onlyChange(aggregate));
    }

    @Test
    void createStage_throws_when_competition_has_started() {
        // A stage already under way -> competition STARTED, so no further stages can be added.
        StageSnapshot startedStage = new StageSnapshot("stage-1", "Stage 1", "comp-1", OWNER, PAST, FUTURE, 0L, 0L, null, List.of());
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, startedStage));
        assertThrows(CompetitionCannotBeUpdatedException.class,
                () -> aggregate.createStage(new NewStageData("s", "S", FUTURE, FUTURE), OWNER, NOW));
    }

    // ---- updateStage ----------------------------------------------------------------------------

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

    @Test
    void updateStage_throws_when_stage_not_found() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage(OWNER, null)));
        assertThrows(StageNotFoundException.class,
                () -> aggregate.updateStage("missing", new StageUpdateData("X", 1L, 2L), OWNER, NOW));
    }

    @Test
    void updateStage_throws_when_stage_is_deleted() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage(OWNER, NOW)));
        assertThrows(StageAlreadyDeletedException.class,
                () -> aggregate.updateStage("stage-1", new StageUpdateData("X", 1L, 2L), OWNER, NOW));
    }

    @Test
    void updateStage_throws_when_user_is_not_stage_creator() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage("other", null)));
        assertThrows(UnauthorizedResourceException.class,
                () -> aggregate.updateStage("stage-1", new StageUpdateData("X", 1L, 2L), OWNER, NOW));
    }

    @Test
    void updateStage_throws_when_stage_is_finished() {
        StageSnapshot finishedStage = new StageSnapshot("stage-1", "Stage 1", "comp-1", OWNER, 0L, 0L, 0L, 0L, null, List.of());
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, finishedStage));
        assertThrows(StageCannotBeUpdatedException.class,
                () -> aggregate.updateStage("stage-1", new StageUpdateData("X", 1L, 2L), OWNER, NOW));
    }

    @Test
    void updateStage_throws_when_date_to_is_before_date_from() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage(OWNER, null)));
        assertThrows(StageDateToBeforeDateFromException.class,
                () -> aggregate.updateStage("stage-1", new StageUpdateData("X", NOW, PAST), OWNER, NOW));
    }

    // ---- deleteStage ----------------------------------------------------------------------------

    @Test
    void updateStage_records_stage_renamed() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, activeStage(OWNER, null)));

        aggregate.updateStage("stage-1", new StageUpdateData("Renamed", 10L, 20L), OWNER, NOW);

        StageUpdated change = assertInstanceOf(StageUpdated.class, onlyChange(aggregate));
        assertEquals("stage-1", change.id());
        assertEquals("Renamed", change.name());
        assertEquals(10L, change.dateFrom());
        assertEquals(20L, change.dateTo());
        assertEquals(NOW, change.lastUpdate());
    }

    @Test
    void updateStage_throws_when_event_enrollment_deadline_not_before_new_dates() {
        StageSnapshot stage = openEnrollmentStage(eventWithEnrollmentDeadline(FUTURE));
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stage));

        assertThrows(EnrollmentDeadlineAfterStageStartException.class,
                () -> aggregate.updateStage("stage-1", new StageUpdateData("Renamed", FUTURE, FUTURE), OWNER, NOW));
    }

    @Test
    void updateStage_records_stage_renamed_when_event_enrollment_deadline_before_new_dates() {
        StageSnapshot stage = openEnrollmentStage(eventWithEnrollmentDeadline(NOW));
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stage));

        aggregate.updateStage("stage-1", new StageUpdateData("Renamed", FUTURE, FUTURE), OWNER, NOW);

        assertInstanceOf(StageUpdated.class, onlyChange(aggregate));
    }

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
                List.of(new Score("ex-1", "judge-1", "dog-1", BigDecimal.TEN, 0L)), List.of(), null, null, null);
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
        EventCompetitor settled = new EventCompetitor("dog-1", "Rex", "Owner", "Handler", "Team", "ES", "Breed", null, null, null,
                (short) 1, null, false, true, null, null, null, null, null);
        EventSnapshot finished = new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(settled), List.of(), List.of(), List.of(), List.of(), null, null, null);
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

    // ---- events ---------------------------------------------------------------------------------

    @Test
    void deleteStage_cascades_soft_delete_to_its_active_events() {
        // dateFrom in the future -> stage is CREATED (deletable); deleting it cascades to its active events.
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(event(null), FUTURE, FUTURE)));

        aggregate.deleteStage("stage-1", OWNER, NOW);

        List<CompetitionChange> changes = aggregate.pendingChanges();
        assertEquals(2, changes.size());
        StageDeleted stageDeleted = assertInstanceOf(StageDeleted.class, changes.get(0));
        assertEquals("stage-1", stageDeleted.id());
        EventDeleted eventDeleted = assertInstanceOf(EventDeleted.class, changes.get(1));
        assertEquals("evt-1", eventDeleted.id());
        assertEquals(NOW, eventDeleted.deletedAt());
    }

    private EventSnapshot event(Long deletedAt) {
        return new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null, 0L, 0L, deletedAt,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), List.of(), null, null, null);
    }

    private EventSnapshot eventWithEnrollmentDeadline(Long enrollmentDeadline) {
        return new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, enrollmentDeadline, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), List.of(), null, null, null);
    }

    private StageSnapshot stageWith(EventSnapshot event, long dateTo) {
        return new StageSnapshot("stage-1", "Stage 1", "comp-1", OWNER, PAST, dateTo, 0L, 0L, null, List.of(event));
    }

    private StageSnapshot stageWith(EventSnapshot event, long dateFrom, long dateTo) {
        return new StageSnapshot("stage-1", "Stage 1", "comp-1", OWNER, dateFrom, dateTo, 0L, 0L, null, List.of(event));
    }

    /**
     * A stage not yet under way (dateFrom in the future), so enrollment is governed solely by the
     * event's own deadline rather than being force-closed by stage status.
     */
    private StageSnapshot openEnrollmentStage(EventSnapshot event) {
        return new StageSnapshot("stage-1", "Stage 1", "comp-1", OWNER, FUTURE, FUTURE, 0L, 0L, null, List.of(event));
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
    void createEvent_throws_when_stage_has_started() {
        // A score on one of the stage's events -> stage STARTED, so it no longer accepts new events.
        EventSnapshot started = new EventSnapshot("evt-1", "cfg-1", "obdx", "Open", "stage-1", OWNER,
                null, 0L, 0L, null, null, List.of(), List.of(), List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", BigDecimal.TEN, 0L)), List.of(), null, null, null);
        StageSnapshot startedStage = new StageSnapshot("stage-1", "Stage 1", "comp-1", OWNER, FUTURE, FUTURE, 0L, 0L, null,
                List.of(started));
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, startedStage));
        assertThrows(EventCannotBeCreatedException.class,
                () -> aggregate.createEvent(new NewEventData("evt-new", "E", "stage-1", "obdx"), OWNER, NOW));
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
        assertThrows(StageExpiredException.class, () -> aggregate.enrollDog("evt-1", "dog-1", false, null, SNAPSHOT, OWNER, NOW));
    }

    @Test
    void enrollDog_throws_when_event_has_no_enrollment_deadline() {
        CompetitionAggregate aggregate =
                CompetitionAggregate.of(competition(OWNER, null, openEnrollmentStage(event(null))));

        assertThrows(EnrollmentClosedException.class, () -> aggregate.enrollDog("evt-1", "dog-1", false, null, SNAPSHOT, OWNER, NOW));
    }

    @Test
    void enrollDog_throws_when_enrollment_deadline_has_passed() {
        CompetitionAggregate aggregate =
                CompetitionAggregate.of(competition(OWNER, null, openEnrollmentStage(eventWithEnrollmentDeadline(PAST))));

        assertThrows(EnrollmentClosedException.class, () -> aggregate.enrollDog("evt-1", "dog-1", false, null, SNAPSHOT, OWNER, NOW));
    }

    @Test
    void enrollDog_records_dog_enrolled() {
        CompetitionAggregate aggregate =
                CompetitionAggregate.of(competition(OWNER, null, openEnrollmentStage(eventWithEnrollmentDeadline(FUTURE))));

        aggregate.enrollDog("evt-1", "dog-1", true, null, SNAPSHOT, OWNER, NOW);

        DogEnrolled change = assertInstanceOf(DogEnrolled.class, onlyChange(aggregate));
        assertEquals("evt-1", change.eventId());
        assertEquals("dog-1", change.dogIdentification());
        assertTrue(change.bih());
        assertEquals((short) 1, change.startNumber());
        assertEquals(SNAPSHOT, change.dogSnapshot());
        assertEquals(NOW, change.lastUpdate());
    }

    @Test
    void enrollDog_assigns_next_start_number_after_last_enrolled_competitor() {
        EventCompetitor existing = new EventCompetitor("dog-1", "dog-1", "o", "h", "t", "c", "b", "i", null, null,
                (short) 3, null, true, false, null, null, null, null, null);
        EventSnapshot eventWithCompetitors = new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, FUTURE,
                0L, 0L, null, ObdxAvgMethod.MID_AVG, List.of(existing), List.of(), List.of(), List.of(), List.of(), null, null, null);
        CompetitionAggregate aggregate =
                CompetitionAggregate.of(competition(OWNER, null, openEnrollmentStage(eventWithCompetitors)));

        aggregate.enrollDog("evt-1", "dog-2", false, null, SNAPSHOT, OWNER, NOW);

        DogEnrolled change = assertInstanceOf(DogEnrolled.class, onlyChange(aggregate));
        assertEquals((short) 4, change.startNumber());
    }

    @Test
    void enrollDog_throws_when_dog_is_already_enrolled() {
        EventCompetitor existing = new EventCompetitor("dog-1", "dog-1", "o", "h", "t", "c", "b", "i", null, null,
                (short) 1, null, true, false, null, null, null, null, null);
        EventSnapshot eventWithCompetitors = new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, FUTURE,
                0L, 0L, null, ObdxAvgMethod.MID_AVG, List.of(existing), List.of(), List.of(), List.of(), List.of(), null, null, null);
        CompetitionAggregate aggregate =
                CompetitionAggregate.of(competition(OWNER, null, openEnrollmentStage(eventWithCompetitors)));

        assertThrows(DogAlreadyEnrolledException.class,
                () -> aggregate.enrollDog("evt-1", "dog-1", false, null, SNAPSHOT, OWNER, NOW));
    }

    @Test
    void updateObdxEventInfo_throws_when_user_is_not_event_creator() {
        EventSnapshot otherEvent = new EventSnapshot("evt-1", null, null, "Event", "stage-1", "other", null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), List.of(), null, null, null);
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(otherEvent, FUTURE)));
        ObdxEventUpdateData data = new ObdxEventUpdateData("E", "cfg", ObdxAvgMethod.MID_AVG, null,
                List.of(), List.of(), List.of(), List.of(), null, null, null);
        assertThrows(UnauthorizedResourceException.class,
                () -> aggregate.updateObdxEventInfo("evt-1", data, OWNER, NOW));
    }

    @Test
    void updateObdxEventInfo_throws_when_event_has_started() {
        // A score recorded on the event -> event is STARTED, so its config is locked even though the stage
        // itself has not reached its start day yet.
        EventSnapshot started = new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", BigDecimal.TEN, 0L)), List.of(), null, null, null);
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(started, FUTURE, FUTURE)));
        ObdxEventUpdateData data = new ObdxEventUpdateData("Event", "cfg-1", ObdxAvgMethod.MID_AVG, 100L,
                List.of(), List.of(), List.of(), List.of(), null, null, null);
        assertThrows(EventCannotBeUpdatedException.class,
                () -> aggregate.updateObdxEventInfo("evt-1", data, OWNER, NOW));
    }

    @Test
    void updateObdxEventInfo_throws_when_event_is_finished() {
        // Stage dateTo already in the past -> event is FINISHED, so its config is locked.
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(event(null), PAST, PAST)));
        ObdxEventUpdateData data = new ObdxEventUpdateData("Event", "cfg-1", ObdxAvgMethod.MID_AVG, null,
                List.of(), List.of(), List.of(), List.of(), null, null, null);
        assertThrows(EventCannotBeUpdatedException.class,
                () -> aggregate.updateObdxEventInfo("evt-1", data, OWNER, NOW));
    }

    /** The booklet has a single "juez principal" box: two flagged judges is an invalid event. */
    @Test
    void updateObdxEventInfo_throws_when_more_than_one_judge_is_the_main_judge() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(event(null), FUTURE, FUTURE)));
        ObdxEventUpdateData data = new ObdxEventUpdateData("Event", "cfg-1", ObdxAvgMethod.MID_AVG, null,
                List.of(), List.of(),
                List.of(new ObdxJudgeItem("judge-1", null, true), new ObdxJudgeItem("judge-2", null, true)),
                List.of(), null, null, null);
        assertThrows(ObdxMultipleMainJudgesException.class,
                () -> aggregate.updateObdxEventInfo("evt-1", data, OWNER, NOW));
    }

    /** One flagged judge, or none, is a valid event. */
    @Test
    void updateObdxEventInfo_accepts_a_single_main_judge() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(event(null), FUTURE, FUTURE)));
        ObdxEventUpdateData data = new ObdxEventUpdateData("Event", "cfg-1", ObdxAvgMethod.MID_AVG, null,
                List.of(), List.of(),
                List.of(new ObdxJudgeItem("judge-1", null, true), new ObdxJudgeItem("judge-2", null, false)),
                List.of(), null, null, null);

        aggregate.updateObdxEventInfo("evt-1", data, OWNER, NOW);

        ObdxEventInfoUpdated change = assertInstanceOf(ObdxEventInfoUpdated.class, onlyChange(aggregate));
        assertEquals(1, change.judges().stream().filter(ObdxJudgeItem::mainJudge).count());
    }

    @Test
    void updateObdxEventInfo_throws_when_enrollment_deadline_is_after_stage_start() {
        // dateFrom in the future so the event is still editable; a deadline after that start day is rejected.
        long afterStart = Instant.parse("2030-01-02T00:00:00Z").toEpochMilli();
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(event(null), FUTURE, FUTURE)));
        ObdxEventUpdateData data = new ObdxEventUpdateData("Event", "cfg-1", ObdxAvgMethod.MID_AVG, afterStart,
                List.of(), List.of(), List.of(), List.of(), null, null, null);
        assertThrows(EnrollmentDeadlineAfterStageStartException.class,
                () -> aggregate.updateObdxEventInfo("evt-1", data, OWNER, NOW));
    }

    @Test
    void updateObdxEventInfo_throws_when_enrollment_deadline_is_on_stage_start_day() {
        // Deadline must be at least the day before dateFrom: the same UTC day as the (future) start is not allowed.
        long sameDayAsDateFrom = Instant.parse("2030-01-01T23:00:00Z").toEpochMilli();
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(event(null), FUTURE, FUTURE)));
        ObdxEventUpdateData data = new ObdxEventUpdateData("Event", "cfg-1", ObdxAvgMethod.MID_AVG, sameDayAsDateFrom,
                List.of(), List.of(), List.of(), List.of(), null, null, null);
        assertThrows(EnrollmentDeadlineAfterStageStartException.class,
                () -> aggregate.updateObdxEventInfo("evt-1", data, OWNER, NOW));
    }

    @Test
    void updateObdxEventInfo_records_obdx_event_info_updated() {
        // dateFrom in the future -> stage not started, event config still editable.
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(event(null), FUTURE, FUTURE)));
        ObdxEventUpdateData data = new ObdxEventUpdateData("Event", "cfg-1", ObdxAvgMethod.MID_AVG, 100L,
                List.of(), List.of(), List.of(), List.of(), 700, null, null);

        aggregate.updateObdxEventInfo("evt-1", data, OWNER, NOW);

        ObdxEventInfoUpdated change = assertInstanceOf(ObdxEventInfoUpdated.class, onlyChange(aggregate));
        assertEquals("evt-1", change.eventId());
        assertEquals("cfg-1", change.configurationId());
        assertEquals(NOW, change.lastUpdate());
        assertEquals(700, change.rankScore().intValue());
    }

    @Test
    void updateObdxEventInfo_snapshots_the_dog_of_a_competitor_joining_now() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(event(null), FUTURE, FUTURE)));
        ObdxEventUpdateData data = new ObdxEventUpdateData("Event", "cfg-1", ObdxAvgMethod.MID_AVG, 100L,
                List.of(new ObdxCompetitorItem("dog-1", (short) 1, null, false, null, false, SNAPSHOT)),
                List.of(), List.of(), List.of(), 700, null, null);

        aggregate.updateObdxEventInfo("evt-1", data, OWNER, NOW);

        ObdxEventInfoUpdated change = assertInstanceOf(ObdxEventInfoUpdated.class, onlyChange(aggregate));
        assertEquals(SNAPSHOT, change.competitors().getFirst().dogSnapshot());
    }

    @Test
    void updateObdxEventInfo_keeps_the_snapshot_an_already_included_competitor_joined_with() {
        CompetitorDogSnapshot atInclusion = new CompetitorDogSnapshot("old handler", "FR", "old team");
        EventCompetitor alreadyIncluded = new EventCompetitor("dog-1", "dog-1", "o", "h", "t", "c", "b", "i", null, null,
                (short) 1, null, true, false, null, null, null, null, atInclusion);
        EventSnapshot eventWithCompetitors = new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null,
                0L, 0L, null, ObdxAvgMethod.MID_AVG, List.of(alreadyIncluded), List.of(), List.of(), List.of(), List.of(), null, null, null);
        CompetitionAggregate aggregate =
                CompetitionAggregate.of(competition(OWNER, null, stageWith(eventWithCompetitors, FUTURE, FUTURE)));
        ObdxEventUpdateData data = new ObdxEventUpdateData("Event", "cfg-1", ObdxAvgMethod.MID_AVG, 100L,
                List.of(new ObdxCompetitorItem("dog-1", (short) 1, null, false, null, false, SNAPSHOT)),
                List.of(), List.of(), List.of(), 700, null, null);

        aggregate.updateObdxEventInfo("evt-1", data, OWNER, NOW);

        ObdxEventInfoUpdated change = assertInstanceOf(ObdxEventInfoUpdated.class, onlyChange(aggregate));
        assertEquals(atInclusion, change.competitors().getFirst().dogSnapshot());
    }

    /**
     * Rows created before the snapshot existed carry no state to preserve, so the update fills them in.
     */
    @Test
    void updateObdxEventInfo_fills_in_an_empty_snapshot_of_an_already_included_competitor() {
        EventCompetitor alreadyIncluded = new EventCompetitor("dog-1", "dog-1", "o", "h", "t", "c", "b", "i", null, null,
                (short) 1, null, true, false, null, null, null, null, CompetitorDogSnapshot.EMPTY);
        EventSnapshot eventWithCompetitors = new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null,
                0L, 0L, null, ObdxAvgMethod.MID_AVG, List.of(alreadyIncluded), List.of(), List.of(), List.of(), List.of(), null, null, null);
        CompetitionAggregate aggregate =
                CompetitionAggregate.of(competition(OWNER, null, stageWith(eventWithCompetitors, FUTURE, FUTURE)));
        ObdxEventUpdateData data = new ObdxEventUpdateData("Event", "cfg-1", ObdxAvgMethod.MID_AVG, 100L,
                List.of(new ObdxCompetitorItem("dog-1", (short) 1, null, false, null, false, SNAPSHOT)),
                List.of(), List.of(), List.of(), 700, null, null);

        aggregate.updateObdxEventInfo("evt-1", data, OWNER, NOW);

        ObdxEventInfoUpdated change = assertInstanceOf(ObdxEventInfoUpdated.class, onlyChange(aggregate));
        assertEquals(SNAPSHOT, change.competitors().getFirst().dogSnapshot());
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
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(scoreEvent(), FUTURE)));
        ScoreUpdateData data = new ScoreUpdateData("judge-1", "ex-1", "dog-1", BigDecimal.TEN);

        aggregate.updateScore("evt-1", data, OWNER, NOW);

        ScoreUpdated change = assertInstanceOf(ScoreUpdated.class, onlyChange(aggregate));
        assertEquals("evt-1", change.eventId());
        assertEquals("judge-1", change.judgeId());
        assertEquals("dog-1", change.dogIdentification());
        assertEquals(BigDecimal.TEN, change.score());
    }

    @Test
    void updateScore_throws_when_judge_is_not_assigned_to_the_exercise() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(scoreEvent(), FUTURE)));
        ScoreUpdateData data = new ScoreUpdateData("judge-2", "ex-1", "dog-1", BigDecimal.TEN);

        assertThrows(ExerciseJudgeNotAssignedException.class, () -> aggregate.updateScore("evt-1", data, OWNER, NOW));
    }

    @Test
    void updateScore_throws_when_competitor_is_disqualified() {
        EventSnapshot disqualified = new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), scoreExercises(), List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", null, 0L, 1000L),
                        new Score("ex-2", "judge-1", "dog-1", null, 0L, 2000L)), List.of(), null, null, null);
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(disqualified, FUTURE)));
        ScoreUpdateData data = new ScoreUpdateData("judge-1", "ex-3", "dog-1", BigDecimal.TEN);

        assertThrows(CompetitorDisqualifiedException.class, () -> aggregate.updateScore("evt-1", data, OWNER, NOW));
    }

    @Test
    void updateScore_throws_when_competitor_is_not_competing() {
        EventCompetitor notCompeting = new EventCompetitor("dog-1", "Rex", "Owner", "Handler", "Team", "ES", "Breed",
                null, null, null, (short) 1, null, true, true, null, null, null, null, null);
        EventSnapshot event = new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(notCompeting), scoreExercises(), List.of(), List.of(), List.of(), null, null, null);
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(event, FUTURE)));
        ScoreUpdateData data = new ScoreUpdateData("judge-1", "ex-1", "dog-1", BigDecimal.TEN);

        assertThrows(CompetitorNotCompetingException.class, () -> aggregate.updateScore("evt-1", data, OWNER, NOW));
    }

    @Test
    void registerYellowCard_records_yellow_card_registered() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(cardEvent(), FUTURE)));
        YellowCardData data = new YellowCardData("judge-1", "ex-1", "dog-1");

        aggregate.registerYellowCard("evt-1", data, OWNER, NOW);

        YellowCardRegistered change = assertInstanceOf(YellowCardRegistered.class, onlyChange(aggregate));
        assertEquals("evt-1", change.eventId());
        assertEquals("judge-1", change.judgeId());
        assertEquals("ex-1", change.exerciseId());
        assertEquals("dog-1", change.dogIdentification());
    }

    @Test
    void registerYellowCard_throws_when_judge_is_not_assigned_to_the_exercise() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(cardEvent(), FUTURE)));
        YellowCardData data = new YellowCardData("judge-2", "ex-1", "dog-1");

        assertThrows(ExerciseJudgeNotAssignedException.class,
                () -> aggregate.registerYellowCard("evt-1", data, OWNER, NOW));
    }

    @Test
    void registerYellowCard_throws_when_already_registered_for_judge_exercise_and_dog() {
        EventSnapshot carded = new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), cardExercises(), List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", null, 0L, 1000L)), List.of(), null, null, null);
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(carded, FUTURE)));
        YellowCardData data = new YellowCardData("judge-1", "ex-1", "dog-1");

        assertThrows(YellowCardAlreadyRegisteredException.class,
                () -> aggregate.registerYellowCard("evt-1", data, OWNER, NOW));
    }

    @Test
    void registerYellowCard_also_registers_red_card_when_it_is_the_second_yellow_card() {
        EventSnapshot event = new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), cardExercises(), List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", null, 0L, 1000L)), List.of(), null, null, null);
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(event, FUTURE)));
        YellowCardData data = new YellowCardData("judge-2", "ex-2", "dog-1");

        aggregate.registerYellowCard("evt-1", data, OWNER, NOW);

        assertEquals(2, aggregate.pendingChanges().size());
        assertInstanceOf(YellowCardRegistered.class, aggregate.pendingChanges().get(0));
        RedCardRegistered redCard = assertInstanceOf(RedCardRegistered.class, aggregate.pendingChanges().get(1));
        assertEquals("evt-1", redCard.eventId());
        assertEquals("judge-2", redCard.judgeId());
        assertEquals("ex-2", redCard.exerciseId());
        assertEquals("dog-1", redCard.dogIdentification());
    }

    @Test
    void registerYellowCard_does_not_duplicate_red_card_when_already_registered() {
        EventSnapshot event = new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), cardExercises(), List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", null, 0L, 1000L, null),
                        new Score("ex-1", "judge-1", "dog-1", null, 0L, null, 500L)), List.of(), null, null, null);
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(event, FUTURE)));
        YellowCardData data = new YellowCardData("judge-2", "ex-2", "dog-1");

        aggregate.registerYellowCard("evt-1", data, OWNER, NOW);

        assertInstanceOf(YellowCardRegistered.class, onlyChange(aggregate));
    }

    @Test
    void registerRedCard_records_red_card_registered() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(cardEvent(), FUTURE)));
        RedCardData data = new RedCardData("judge-1", "ex-1", "dog-1");

        aggregate.registerRedCard("evt-1", data, OWNER, NOW);

        RedCardRegistered change = assertInstanceOf(RedCardRegistered.class, onlyChange(aggregate));
        assertEquals("evt-1", change.eventId());
        assertEquals("judge-1", change.judgeId());
        assertEquals("ex-1", change.exerciseId());
        assertEquals("dog-1", change.dogIdentification());
    }

    @Test
    void registerRedCard_throws_when_judge_is_not_assigned_to_the_exercise() {
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(cardEvent(), FUTURE)));
        RedCardData data = new RedCardData("judge-2", "ex-1", "dog-1");

        assertThrows(ExerciseJudgeNotAssignedException.class,
                () -> aggregate.registerRedCard("evt-1", data, OWNER, NOW));
    }

    @Test
    void registerRedCard_throws_when_already_registered() {
        EventSnapshot carded = new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), cardExercises(), List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", null, 0L, null, 1000L)), List.of(), null, null, null);
        CompetitionAggregate aggregate = CompetitionAggregate.of(competition(OWNER, null, stageWith(carded, FUTURE)));
        RedCardData data = new RedCardData("judge-1", "ex-1", "dog-1");

        assertThrows(RedCardAlreadyRegisteredException.class,
                () -> aggregate.registerRedCard("evt-1", data, OWNER, NOW));
    }

    /**
     * Event whose exercises assign judge-1 to ex-1 and judge-2 to ex-2, so card registrations for those
     * pairs pass the judge-assignment check.
     */
    private EventSnapshot cardEvent() {
        return new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), cardExercises(), List.of(), List.of(), List.of(), null, null, null);
    }

    private static List<EventExercise> cardExercises() {
        return List.of(
                new EventExercise("ex-1", (short) 1, List.of(), List.of("judge-1")),
                new EventExercise("ex-2", (short) 2, List.of(), List.of("judge-2")));
    }

    /**
     * Event whose exercises all assign judge-1, so score registrations for judge-1 on ex-1/ex-2/ex-3 pass the
     * judge-assignment check.
     */
    private EventSnapshot scoreEvent() {
        return new EventSnapshot("evt-1", null, null, "Event", "stage-1", OWNER, null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), scoreExercises(), List.of(), List.of(), List.of(), null, null, null);
    }

    private static List<EventExercise> scoreExercises() {
        return List.of(
                new EventExercise("ex-1", (short) 1, List.of(), List.of("judge-1")),
                new EventExercise("ex-2", (short) 2, List.of(), List.of("judge-1")),
                new EventExercise("ex-3", (short) 3, List.of(), List.of("judge-1")));
    }

    // ---- notifiableStageName / assertEventNotifiableBy ------------------------------------------------

    private EventSnapshot notifiableEvent(String id, String creator, Long deletedAt) {
        return new EventSnapshot(id, null, null, "Event " + id, "stage-1", creator, null, 0L, 0L, deletedAt,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), List.of(), null, null, null);
    }

    private CompetitionAggregate withStageEvents(Long stageDeletedAt, EventSnapshot... events) {
        return withStageEvents(stageDeletedAt, FUTURE, events);
    }

    private CompetitionAggregate withStageEvents(Long stageDeletedAt, long stageDateTo, EventSnapshot... events) {
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", OWNER, FUTURE, stageDateTo, 0L, 0L,
                stageDeletedAt, List.of(events));
        return CompetitionAggregate.of(competition(OWNER, null, stage));
    }

    @Test
    void notifiableStageName_returns_the_stage_name() {
        CompetitionAggregate aggregate = withStageEvents(null, notifiableEvent("evt-1", OWNER, null));

        assertEquals("Stage 1", aggregate.notifiableStageName("stage-1", NOW));
    }

    @Test
    void notifiableStageName_throws_when_stage_is_unknown() {
        CompetitionAggregate aggregate = withStageEvents(null, notifiableEvent("evt-1", OWNER, null));

        assertThrows(StageNotFoundException.class, () -> aggregate.notifiableStageName("stage-unknown", NOW));
    }

    @Test
    void notifiableStageName_throws_when_stage_is_deleted() {
        CompetitionAggregate aggregate = withStageEvents(NOW, notifiableEvent("evt-1", OWNER, null));

        assertThrows(StageAlreadyDeletedException.class, () -> aggregate.notifiableStageName("stage-1", NOW));
    }

    @Test
    void notifiableStageName_throws_when_stage_has_finished() {
        CompetitionAggregate aggregate = withStageEvents(null, PAST, notifiableEvent("evt-1", OWNER, null));

        assertThrows(StageFinishedException.class, () -> aggregate.notifiableStageName("stage-1", NOW));
    }

    @Test
    void assertEventNotifiableBy_passes_for_an_active_event_of_the_stage_created_by_the_user() {
        CompetitionAggregate aggregate = withStageEvents(null, notifiableEvent("evt-1", OWNER, null));

        assertDoesNotThrow(() -> aggregate.assertEventNotifiableBy("evt-1", "stage-1", OWNER, NOW));
    }

    @Test
    void assertEventNotifiableBy_throws_when_event_belongs_to_another_stage() {
        CompetitionAggregate aggregate = withStageEvents(null, notifiableEvent("evt-1", OWNER, null));

        assertThrows(EventNotInStageException.class,
                () -> aggregate.assertEventNotifiableBy("evt-1", "stage-2", OWNER, NOW));
    }

    @Test
    void assertEventNotifiableBy_throws_when_event_is_unknown() {
        CompetitionAggregate aggregate = withStageEvents(null, notifiableEvent("evt-1", OWNER, null));

        assertThrows(EventNotFoundException.class,
                () -> aggregate.assertEventNotifiableBy("evt-unknown", "stage-1", OWNER, NOW));
    }

    @Test
    void assertEventNotifiableBy_throws_when_event_is_deleted() {
        CompetitionAggregate aggregate = withStageEvents(null, notifiableEvent("evt-1", OWNER, NOW));

        assertThrows(EventAlreadyDeletedException.class,
                () -> aggregate.assertEventNotifiableBy("evt-1", "stage-1", OWNER, NOW));
    }

    @Test
    void assertEventNotifiableBy_throws_when_user_did_not_create_the_event() {
        CompetitionAggregate aggregate = withStageEvents(null, notifiableEvent("evt-1", "other-user", null));

        assertThrows(UnauthorizedResourceException.class,
                () -> aggregate.assertEventNotifiableBy("evt-1", "stage-1", OWNER, NOW));
    }

    @Test
    void assertEventNotifiableBy_throws_when_the_event_has_finished() {
        CompetitionAggregate aggregate = withStageEvents(null, PAST, notifiableEvent("evt-1", OWNER, null));

        assertThrows(EventFinishedException.class,
                () -> aggregate.assertEventNotifiableBy("evt-1", "stage-1", OWNER, NOW));
    }

    @Test
    void assertEventSubscribable_passes_for_an_active_running_event_of_any_user() {
        CompetitionAggregate aggregate = withStageEvents(null, notifiableEvent("evt-1", "other-user", null));

        assertDoesNotThrow(() -> aggregate.assertEventSubscribable("evt-1", NOW));
    }

    @Test
    void assertEventSubscribable_throws_when_the_event_has_finished() {
        CompetitionAggregate aggregate = withStageEvents(null, PAST, notifiableEvent("evt-1", OWNER, null));

        assertThrows(EventFinishedException.class, () -> aggregate.assertEventSubscribable("evt-1", NOW));
    }

    @Test
    void assertEventSubscribable_throws_when_event_is_unknown() {
        CompetitionAggregate aggregate = withStageEvents(null, notifiableEvent("evt-1", OWNER, null));

        assertThrows(EventNotFoundException.class, () -> aggregate.assertEventSubscribable("evt-unknown", NOW));
    }

    @Test
    void assertEventSubscribable_throws_when_event_is_deleted() {
        CompetitionAggregate aggregate = withStageEvents(null, notifiableEvent("evt-1", OWNER, NOW));

        assertThrows(EventAlreadyDeletedException.class, () -> aggregate.assertEventSubscribable("evt-1", NOW));
    }

}
