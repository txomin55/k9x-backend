package com.k9x.domain.events.status;

import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.valueobjects.EventCompetitor;
import com.k9x.domain.events.valueobjects.EventExercise;
import com.k9x.domain.events.valueobjects.EventJudge;
import com.k9x.domain.events.valueobjects.Score;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventStatusTest {

    private static EventSnapshot event(Long deletedAt, List<EventCompetitor> competitors,
                               List<EventExercise> exercises, List<EventJudge> judges, List<Score> scores) {
        return new EventSnapshot("e1", "cfg", "obdx", "Event", "s1", "creator", null, 0L, 0L, deletedAt,
                ObdxAvgMethod.AVG, competitors, exercises, judges, scores);
    }

    private static EventCompetitor competitor(String dogId, boolean notCompeting) {
        return new EventCompetitor(dogId, dogId, "o", "h", "t", "c", "b", "i", (short) 0, true, notCompeting);
    }

    private static EventExercise exercise(String id) {
        return new EventExercise(id, (short) 1, List.of());
    }

    private static EventJudge judge(String id) {
        return new EventJudge(id, id, null);
    }

    private static Score score(String exerciseId, String judgeId, String dogId, BigDecimal value) {
        return new Score(exerciseId, judgeId, dogId, value, 0L);
    }

    @Test
    void deleted_when_deleted_at_is_set() {
        assertEquals(EventStatus.DELETED,
                event(1L, List.of(competitor("d1", false)), List.of(exercise("x1")), List.of(judge("j1")), List.of()).status());
    }

    @Test
    void created_when_competitors_exist_but_no_scores() {
        assertEquals(EventStatus.CREATED,
                event(null, List.of(competitor("d1", false)), List.of(exercise("x1")), List.of(judge("j1")), List.of()).status());
    }

    @Test
    void started_when_at_least_one_score_but_not_all_settled() {
        // two exercises required per competitor, only one scored
        EventSnapshot e = event(null, List.of(competitor("d1", false)),
                List.of(exercise("x1"), exercise("x2")), List.of(judge("j1")),
                List.of(score("x1", "j1", "d1", new BigDecimal("8.0"))));
        assertEquals(EventStatus.STARTED, e.status());
    }

    @Test
    void finished_when_every_competitor_has_a_score_for_every_exercise_judge_pair() {
        EventSnapshot e = event(null, List.of(competitor("d1", false)),
                List.of(exercise("x1")), List.of(judge("j1")),
                List.of(score("x1", "j1", "d1", new BigDecimal("8.0"))));
        assertEquals(EventStatus.FINISHED, e.status());
    }

    @Test
    void finished_when_competitor_is_marked_not_competing() {
        EventSnapshot e = event(null, List.of(competitor("d1", true)),
                List.of(exercise("x1")), List.of(judge("j1")), List.of());
        assertEquals(EventStatus.FINISHED, e.status());
    }

    @Test
    void not_finished_when_there_are_no_competitors_even_if_a_score_exists() {
        EventSnapshot e = event(null, List.of(), List.of(exercise("x1")), List.of(judge("j1")),
                List.of(score("x1", "j1", "d1", new BigDecimal("8.0"))));
        assertEquals(EventStatus.STARTED, e.status());
    }
}
