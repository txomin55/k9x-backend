package com.k9x.domain.disciplines.obdx;

import com.k9x.domain.disciplines.obdx.exceptions.ObdxNotEnoughJudgesException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Two different counts govern MID_AVG and mixing them up is the easy mistake: the <em>panel</em> decides
 * whether the method is legal at all, and how many judges <em>actually scored</em> decides whether there is
 * anything to trim.
 *
 * <p>The shape that forced the distinction is the world championship semifinal: four judges split across two
 * rings, so the two group exercises carry four scores and each individual exercise only two. Trimming a high
 * and a low out of two scores would leave nothing, so those must fall back to a plain mean — without the event
 * stopping being a four-judge, MID_AVG trial.
 */
class ObdxScoreAveragingTest {

    private static List<BigDecimal> scores(String... values) {
        return List.of(values).stream().map(BigDecimal::new).toList();
    }

    private static void assertAverage(String expected, List<BigDecimal> scores, ObdxAvgMethod method, int panel) {
        BigDecimal actual = ObdxScoreAveraging.average(scores, method, panel);
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> "esperado " + expected + " y salió " + actual);
    }

    @Test
    void trims_the_high_and_the_low_when_four_judges_scored() {
        assertAverage("9.75", scores("10.0", "10.0", "9.0", "9.5"), ObdxAvgMethod.MID_AVG, 4);
    }

    @Test
    void averages_the_two_scores_when_only_two_judges_scored_that_exercise() {
        // The exercise of a ring: the panel is four, but only its two judges scored here. Nothing to trim.
        assertAverage("9.25", scores("8.5", "10.0"), ObdxAvgMethod.MID_AVG, 4);
    }

    @Test
    void a_single_score_with_a_full_panel_is_that_score() {
        assertAverage("7.5", scores("7.5"), ObdxAvgMethod.MID_AVG, 4);
    }

    @Test
    void rejects_mid_avg_when_the_panel_itself_is_too_small() {
        // Here there is no reading under which trimming makes sense: the trial only had two judges.
        assertThrows(ObdxNotEnoughJudgesException.class,
                () -> ObdxScoreAveraging.average(scores("8.5", "10.0"), ObdxAvgMethod.MID_AVG, 2));
    }

    @Test
    void avg_never_trims_and_never_asks_about_the_panel() {
        assertAverage("9.625", scores("10.0", "10.0", "9.0", "9.5"), ObdxAvgMethod.AVG, 4);
        assertAverage("9.25", scores("8.5", "10.0"), ObdxAvgMethod.AVG, 0);
    }

    @Test
    void an_exercise_nobody_scored_is_zero() {
        assertAverage("0", List.of(), ObdxAvgMethod.MID_AVG, 4);
    }

    @Test
    void the_panel_minimum_is_about_the_event_not_the_exercise() {
        assertTrue(ObdxScoreAveraging.hasEnoughJudges(ObdxAvgMethod.MID_AVG, 4));
        assertFalse(ObdxScoreAveraging.hasEnoughJudges(ObdxAvgMethod.MID_AVG, 3));
        assertTrue(ObdxScoreAveraging.hasEnoughJudges(ObdxAvgMethod.AVG, 1));
    }
}
