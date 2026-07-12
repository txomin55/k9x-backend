package com.k9x.domain.disciplines.obdx;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupExerciseTest {

    @Test
    void recognizes_configured_group_exercise_ids() {
        assertTrue(GroupExercise.isGroupExercise("OBDX_CPC_COBS.7_V0"));
        assertTrue(GroupExercise.isGroupExercise("OBDX_RSCE_GRADE_1.1_V0"));
        assertTrue(GroupExercise.isGroupExercise("OBDX_FCI_GRADE_3.1_V0"));
        assertTrue(GroupExercise.isGroupExercise("OBDX_FCI_GRADE_3.2_V0"));
        assertTrue(GroupExercise.isGroupExercise("OBDX_FCI_GRADE_2.1_V0"));
    }

    @Test
    void recognizes_group_exercise_across_config_versions() {
        assertTrue(GroupExercise.isGroupExercise("OBDX_FCI_GRADE_2.1_V1"));
        assertTrue(GroupExercise.isGroupExercise("OBDX_FCI_GRADE_2.1_V12"));
    }

    @Test
    void does_not_recognize_individual_exercise_ids() {
        assertFalse(GroupExercise.isGroupExercise("OBDX_FCI_GRADE_3.3_V0"));
        assertFalse(GroupExercise.isGroupExercise("OBDX_CPC_COBS.1_V0"));
        assertFalse(GroupExercise.isGroupExercise("ex-1"));
        assertFalse(GroupExercise.isGroupExercise(null));
    }
}
