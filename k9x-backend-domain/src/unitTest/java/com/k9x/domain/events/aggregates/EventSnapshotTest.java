package com.k9x.domain.events.aggregates;

import com.k9x.domain.events.status.EventStatus;
import com.k9x.domain.events.valueobjects.EventCompetitor;
import com.k9x.domain.events.valueobjects.EventExercise;
import com.k9x.domain.events.valueobjects.Score;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventSnapshotTest {

    private static final long FUTURE = Instant.parse("2030-01-01T00:00:00Z").toEpochMilli();
    private static final String DOG = "dog-1";
    private static final String JUDGE = "judge-1";
    private static final String INDIVIDUAL_1 = "OBDX_FCI_GRADE_2.2_V0";
    private static final String INDIVIDUAL_2 = "OBDX_FCI_GRADE_2.9_V0";
    private static final String GROUP_STAY = "OBDX_FCI_GRADE_2.1_V0";
    private static final String GENERAL_IMPRESSION = "OBDX_FCI_GRADE_2.10_V0";

    private EventSnapshot event(List<EventExercise> exercises, List<Score> scores) {
        EventCompetitor competitor = new EventCompetitor(DOG, "Rex", null, null, null, null, null, null,
                (short) 1, null, null, false, null, null, null);
        return new EventSnapshot("evt-1", "cfg-1", "obdx", "Event", "stage-1", "user-1", null,
                0L, 0L, null, null, List.of(competitor), exercises, List.of(), scores, List.of(), null, null);
    }

    private Score score(String exerciseId) {
        return new Score(exerciseId, JUDGE, DOG, BigDecimal.TEN, 0L);
    }

    private EventExercise exercise(String exerciseId) {
        return new EventExercise(exerciseId, (short) 0, List.of(), List.of(JUDGE));
    }

    // ---- isCompetitorStarted -------------------------------------------------------------------

    @Test
    void competitor_started_when_scored_on_individual_exercise() {
        assertTrue(event(List.of(exercise(INDIVIDUAL_1)), List.of(score(INDIVIDUAL_1)))
                .isCompetitorStarted(DOG));
    }

    @Test
    void competitor_not_started_when_only_scored_on_group_stay() {
        assertFalse(event(List.of(exercise(GROUP_STAY)), List.of(score(GROUP_STAY)))
                .isCompetitorStarted(DOG));
    }

    @Test
    void competitor_not_started_when_only_scored_on_general_impression() {
        assertFalse(event(List.of(exercise(GENERAL_IMPRESSION)), List.of(score(GENERAL_IMPRESSION)))
                .isCompetitorStarted(DOG));
    }

    // ---- isCompetitorSettled -------------------------------------------------------------------

    @Test
    void competitor_settled_when_all_individual_exercises_scored_even_if_group_and_impression_pending() {
        List<EventExercise> exercises = List.of(
                exercise(INDIVIDUAL_1), exercise(INDIVIDUAL_2), exercise(GROUP_STAY), exercise(GENERAL_IMPRESSION));
        List<Score> scores = List.of(score(INDIVIDUAL_1), score(INDIVIDUAL_2));

        assertTrue(event(exercises, scores).isCompetitorSettled(DOG));
    }

    @Test
    void competitor_not_settled_while_an_individual_exercise_is_unscored() {
        List<EventExercise> exercises = List.of(
                exercise(INDIVIDUAL_1), exercise(INDIVIDUAL_2), exercise(GROUP_STAY));
        List<Score> scores = List.of(score(INDIVIDUAL_1));

        assertFalse(event(exercises, scores).isCompetitorSettled(DOG));
    }

    // ---- event status --------------------------------------------------------------------------

    @Test
    void event_not_finished_while_group_or_impression_pending_even_if_competitor_settled() {
        List<EventExercise> exercises = List.of(
                exercise(INDIVIDUAL_1), exercise(INDIVIDUAL_2), exercise(GROUP_STAY), exercise(GENERAL_IMPRESSION));
        List<Score> scores = List.of(score(INDIVIDUAL_1), score(INDIVIDUAL_2));
        EventSnapshot event = event(exercises, scores);

        assertTrue(event.isCompetitorSettled(DOG));
        assertEquals(EventStatus.STARTED, event.status(0L, FUTURE));
    }

    @Test
    void event_started_when_only_scored_on_group_stay_even_though_competitor_not_started() {
        List<EventExercise> exercises = List.of(
                exercise(INDIVIDUAL_1), exercise(GROUP_STAY), exercise(GENERAL_IMPRESSION));
        List<Score> scores = List.of(score(GROUP_STAY));
        EventSnapshot event = event(exercises, scores);

        assertFalse(event.isCompetitorStarted(DOG));
        assertEquals(EventStatus.STARTED, event.status(0L, FUTURE));
    }

    @Test
    void event_started_when_only_scored_on_general_impression_even_though_competitor_not_started() {
        List<EventExercise> exercises = List.of(
                exercise(INDIVIDUAL_1), exercise(GROUP_STAY), exercise(GENERAL_IMPRESSION));
        List<Score> scores = List.of(score(GENERAL_IMPRESSION));
        EventSnapshot event = event(exercises, scores);

        assertFalse(event.isCompetitorStarted(DOG));
        assertEquals(EventStatus.STARTED, event.status(0L, FUTURE));
    }

    @Test
    void event_finished_only_when_all_exercises_including_group_and_impression_scored() {
        List<EventExercise> exercises = List.of(
                exercise(INDIVIDUAL_1), exercise(INDIVIDUAL_2), exercise(GROUP_STAY), exercise(GENERAL_IMPRESSION));
        List<Score> scores = List.of(
                score(INDIVIDUAL_1), score(INDIVIDUAL_2), score(GROUP_STAY), score(GENERAL_IMPRESSION));

        assertEquals(EventStatus.FINISHED, event(exercises, scores).status(0L, FUTURE));
    }
}
