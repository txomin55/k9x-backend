package com.k9x.domain.disciplines.obdx;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObdxFinalScoreExerciseTest {

    @Test
    void recognises_the_final_score_exercise() {
        assertTrue(ObdxFinalScoreExercise.isFinalScore("OBDX.FINAL_SCORE"));
    }

    @Test
    void recognises_the_final_score_exercise_ignoring_its_version_suffix() {
        assertTrue(ObdxFinalScoreExercise.isFinalScore("OBDX.FINAL_SCORE_V0"));
        assertTrue(ObdxFinalScoreExercise.isFinalScore("OBDX.FINAL_SCORE_V12"));
    }

    @Test
    void does_not_recognise_a_graded_exercise() {
        assertFalse(ObdxFinalScoreExercise.isFinalScore("OBDX.FCI_GRADE_3.1_V0"));
    }

    @Test
    void does_not_recognise_a_null_exercise() {
        assertFalse(ObdxFinalScoreExercise.isFinalScore(null));
    }
}
