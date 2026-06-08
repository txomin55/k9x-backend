package com.k9x.domain.aggregates.stages;

import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.events.EventCompetitor;
import com.k9x.domain.aggregates.events.EventExercise;
import com.k9x.domain.aggregates.events.EventJudge;
import com.k9x.domain.aggregates.events.Score;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StageStatusTest {

    private static final long NOW = Instant.parse("2024-06-15T12:00:00Z").toEpochMilli();
    private static final long TODAY = Instant.parse("2024-06-15T08:00:00Z").toEpochMilli();
    private static final long YESTERDAY = Instant.parse("2024-06-14T08:00:00Z").toEpochMilli();
    private static final long TOMORROW = Instant.parse("2024-06-16T08:00:00Z").toEpochMilli();
    private static final long NEXT_WEEK = Instant.parse("2024-06-22T08:00:00Z").toEpochMilli();

    private static Stage stage(long dateFrom, long dateTo, Long deletedAt, List<Event> events) {
        return new Stage("s1", "Stage", "c1", "creator", dateFrom, dateTo, 0L, 0L, deletedAt, events);
    }

    private static Event startedEvent() {
        // one competitor, two exercises (so not all settled), one score -> STARTED
        return new Event("e1", "cfg", "obdx", "Event", "s1", "creator", null, 0L, 0L, null, ObdxAvgMethod.AVG,
                List.of(new EventCompetitor("d1", "d1", "o", "t", "c", "b", "i", (short) 0, true, false)),
                List.of(new EventExercise("x1", (short) 1, List.of()), new EventExercise("x2", (short) 2, List.of())),
                List.of(new EventJudge("j1", "j1", null)),
                List.of(new Score("x1", "j1", "d1", new BigDecimal("8.0"), 0L)));
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
    void created_when_date_from_is_a_future_day() {
        assertEquals(StageStatus.CREATED, stage(NEXT_WEEK, NEXT_WEEK, null, List.of()).status(NOW));
    }

    @Test
    void finished_takes_precedence_over_started_events_once_the_day_passed() {
        assertEquals(StageStatus.FINISHED, stage(YESTERDAY, YESTERDAY, null, List.of(startedEvent())).status(NOW));
    }
}
