package com.k9x.domain.disciplines.obdx;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiveExcludedExerciseTest {

    @Test
    void excludes_configured_group_stays() {
        assertTrue(LiveExcludedExercise.isExcluded("OBDX.CPC_COBS.7_V0"));
        assertTrue(LiveExcludedExercise.isExcluded("OBDX.RSCE_GRADE_1.1_V0"));
        assertTrue(LiveExcludedExercise.isExcluded("OBDX.FCI_GRADE_3.1_V0"));
        assertTrue(LiveExcludedExercise.isExcluded("OBDX.FCI_GRADE_3.2_V0"));
        assertTrue(LiveExcludedExercise.isExcluded("OBDX.FCI_GRADE_2.1_V0"));
        assertTrue(LiveExcludedExercise.isExcluded("OBDX.FCI_GRADE_1.1_V0"));
    }

    @Test
    void excludes_configured_general_impression() {
        assertTrue(LiveExcludedExercise.isExcluded("OBDX.FCI_GRADE_1.9_V0"));
        assertTrue(LiveExcludedExercise.isExcluded("OBDX.FCI_GRADE_2.10_V0"));
        assertTrue(LiveExcludedExercise.isExcluded("OBDX.CPC_COBS.8_V0"));
        assertTrue(LiveExcludedExercise.isExcluded("OBDX.RSCE_DEBUTANTE.9_V0"));
    }

    @Test
    void excludes_exercise_across_config_versions() {
        assertTrue(LiveExcludedExercise.isExcluded("OBDX.FCI_GRADE_2.1_V1"));
        assertTrue(LiveExcludedExercise.isExcluded("OBDX.FCI_GRADE_2.10_V12"));
    }

    @Test
    void does_not_exclude_individual_exercises() {
        assertFalse(LiveExcludedExercise.isExcluded("OBDX.FCI_GRADE_3.3_V0"));
        assertFalse(LiveExcludedExercise.isExcluded("OBDX.FCI_GRADE_2.9_V0"));
        assertFalse(LiveExcludedExercise.isExcluded("OBDX.CPC_COBS.1_V0"));
        // general impression not in the enum (RSCE grade 1) must not be excluded
        assertFalse(LiveExcludedExercise.isExcluded("OBDX.RSCE_GRADE_1.10_V0"));
        assertFalse(LiveExcludedExercise.isExcluded("ex-1"));
        assertFalse(LiveExcludedExercise.isExcluded(null));
    }
}
