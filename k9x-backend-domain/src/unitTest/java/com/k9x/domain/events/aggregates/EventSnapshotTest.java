package com.k9x.domain.events.aggregates;

import com.k9x.domain.events.valueobjects.Score;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventSnapshotTest {

    private static final String INDIVIDUAL_EXERCISE = "OBDX_FCI_GRADE_3.3_V0";
    private static final String GROUP_EXERCISE = "OBDX_FCI_GRADE_3.1_V0";

    private EventSnapshot eventWithScores(List<Score> scores) {
        return new EventSnapshot("evt-1", "cfg-1", "obdx", "Event", "stage-1", "user-1", null,
                0L, 0L, null, null, List.of(), List.of(), List.of(), scores, List.of(), null);
    }

    @Test
    void competitor_started_when_scored_on_individual_exercise() {
        EventSnapshot event = eventWithScores(List.of(
                new Score(INDIVIDUAL_EXERCISE, "judge-1", "dog-1", BigDecimal.TEN, 0L)));

        assertTrue(event.isCompetitorStarted("dog-1"));
    }

    @Test
    void competitor_not_started_when_only_scored_on_group_exercise() {
        EventSnapshot event = eventWithScores(List.of(
                new Score(GROUP_EXERCISE, "judge-1", "dog-1", BigDecimal.TEN, 0L)));

        assertFalse(event.isCompetitorStarted("dog-1"));
    }

    @Test
    void competitor_started_when_scored_on_both_group_and_individual() {
        EventSnapshot event = eventWithScores(List.of(
                new Score(GROUP_EXERCISE, "judge-1", "dog-1", BigDecimal.TEN, 0L),
                new Score(INDIVIDUAL_EXERCISE, "judge-1", "dog-1", BigDecimal.TEN, 0L)));

        assertTrue(event.isCompetitorStarted("dog-1"));
    }
}
