package com.k9x.domain.stages.status;

import com.k9x.domain.stages.aggregates.StageSnapshot;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.valueobjects.EventCompetitor;
import com.k9x.domain.events.valueobjects.EventExercise;
import com.k9x.domain.events.valueobjects.EventJudge;
import com.k9x.domain.events.valueobjects.Score;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StageStatusTest {

    private static final long NOW = Instant.parse("2024-06-15T12:00:00Z").toEpochMilli();
    private static final long TODAY = Instant.parse("2024-06-15T08:00:00Z").toEpochMilli();
    private static final long YESTERDAY = Instant.parse("2024-06-14T08:00:00Z").toEpochMilli();
    private static final long TOMORROW = Instant.parse("2024-06-16T08:00:00Z").toEpochMilli();
    private static final long NEXT_WEEK = Instant.parse("2024-06-22T08:00:00Z").toEpochMilli();

    private static StageSnapshot stage(long dateFrom, long dateTo, Long deletedAt, List<EventSnapshot> events) {
        return new StageSnapshot("s1", "Stage", "c1", "creator", dateFrom, dateTo, 0L, 0L, deletedAt, events);
    }

    private static EventSnapshot startedEvent() {
        // one competitor, two exercises (so not all settled), one score -> STARTED
        return new EventSnapshot("e1", "cfg", "obdx", "Event", "s1", "creator", null, 0L, 0L, null, ObdxAvgMethod.AVG,
                List.of(new EventCompetitor("d1", "d1", "o", "h", "t", "c", "b", "i", (short) 0, true, false, null, null, null)),
                List.of(new EventExercise("x1", (short) 1, List.of()), new EventExercise("x2", (short) 2, List.of())),
                List.of(new EventJudge("j1", "j1", null)),
                List.of(new Score("x1", "j1", "d1", new BigDecimal("8.0"), 0L)), List.of());
    }

    private static EventSnapshot openEvent() {
        // no scores -> CREATED, deadline not yet reached -> enrollment open on its own
        return new EventSnapshot("e2", "cfg", "obdx", "Event", "s1", "creator", NEXT_WEEK, 0L, 0L, null,
                ObdxAvgMethod.AVG, List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private static EventSnapshot noDeadlineEvent() {
        // no scores -> CREATED, no deadline set -> enrollment never open
        return new EventSnapshot("e4", "cfg", "obdx", "Event", "s1", "creator", null, 0L, 0L, null, ObdxAvgMethod.AVG,
                List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private static EventSnapshot finishedEvent() {
        // single competitor flagged notCompeting -> settled -> FINISHED regardless of dateTo
        return new EventSnapshot("e3", "cfg", "obdx", "Event", "s1", "creator", null, 0L, 0L, null, ObdxAvgMethod.AVG,
                List.of(new EventCompetitor("d1", "d1", "o", "h", "t", "c", "b", "i", (short) 0, true, true, null, null, null)),
                List.of(), List.of(), List.of(), List.of());
    }

    @Test
    void enrollment_closed_when_stage_is_to_start() {
        StageSnapshot stage = stage(TODAY, NEXT_WEEK, null, List.of(openEvent()));
        assertEquals(StageStatus.TO_START, stage.status(NOW));
        assertFalse(stage.enrollmentOpened(openEvent(), NOW));
    }

    @Test
    void enrollment_closed_when_stage_is_started() {
        StageSnapshot stage = stage(TODAY, TOMORROW, null, List.of(startedEvent()));
        assertEquals(StageStatus.STARTED, stage.status(NOW));
        assertFalse(stage.enrollmentOpened(openEvent(), NOW));
    }

    @Test
    void enrollment_open_when_stage_not_started_and_event_deadline_not_reached() {
        StageSnapshot stage = stage(NEXT_WEEK, NEXT_WEEK, null, List.of(openEvent()));
        assertEquals(StageStatus.CREATED, stage.status(NOW));
        assertTrue(stage.enrollmentOpened(openEvent(), NOW));
    }

    @Test
    void enrollment_closed_when_event_has_no_deadline() {
        StageSnapshot stage = stage(NEXT_WEEK, NEXT_WEEK, null, List.of(noDeadlineEvent()));
        assertEquals(StageStatus.CREATED, stage.status(NOW));
        assertFalse(stage.enrollmentOpened(noDeadlineEvent(), NOW));
    }

    @Test
    void deleted_when_deleted_at_is_set() {
        assertEquals(StageStatus.DELETED, stage(TODAY, NEXT_WEEK, 1L, List.of()).status(NOW));
    }

    @Test
    void finished_when_now_is_a_day_after_date_to() {
        assertEquals(StageStatus.FINISHED, stage(YESTERDAY, YESTERDAY, null, List.of()).status(NOW));
    }

    @Test
    void started_when_an_event_is_started() {
        assertEquals(StageStatus.STARTED, stage(TODAY, TOMORROW, null, List.of(startedEvent())).status(NOW));
    }

    @Test
    void to_start_when_today_is_the_date_from_day_and_no_event_started() {
        assertEquals(StageStatus.TO_START, stage(TODAY, NEXT_WEEK, null, List.of()).status(NOW));
    }

    @Test
    void to_start_when_today_is_within_a_multi_day_stage_range_and_no_score_recorded() {
        assertEquals(StageStatus.TO_START, stage(YESTERDAY, TOMORROW, null, List.of()).status(NOW));
    }

    @Test
    void created_when_date_from_is_a_future_day() {
        assertEquals(StageStatus.CREATED, stage(NEXT_WEEK, NEXT_WEEK, null, List.of()).status(NOW));
    }

    @Test
    void finished_takes_precedence_over_started_events_once_the_day_passed() {
        assertEquals(StageStatus.FINISHED, stage(YESTERDAY, YESTERDAY, null, List.of(startedEvent())).status(NOW));
    }

    @Test
    void finished_when_all_events_are_finished_even_if_date_to_has_not_passed() {
        assertEquals(StageStatus.FINISHED, stage(TODAY, TOMORROW, null, List.of(finishedEvent())).status(NOW));
    }

    @Test
    void not_finished_when_only_some_events_are_finished() {
        assertEquals(StageStatus.STARTED,
                stage(TODAY, TOMORROW, null, List.of(finishedEvent(), startedEvent())).status(NOW));
    }
}
