package com.k9x.domain.disciplines.obdx;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ObdxRankTest {

    @Test
    void from_score_reads_the_letter_from_the_global_bands() {
        assertEquals(ObdxRank.E, ObdxRank.fromScore(0));
        assertEquals(ObdxRank.E, ObdxRank.fromScore(200));
        assertEquals(ObdxRank.D, ObdxRank.fromScore(201));
        assertEquals(ObdxRank.D, ObdxRank.fromScore(400));
        assertEquals(ObdxRank.C, ObdxRank.fromScore(401));
        assertEquals(ObdxRank.C, ObdxRank.fromScore(600));
        assertEquals(ObdxRank.B, ObdxRank.fromScore(601));
        assertEquals(ObdxRank.B, ObdxRank.fromScore(800));
        assertEquals(ObdxRank.A, ObdxRank.fromScore(801));
        assertEquals(ObdxRank.A, ObdxRank.fromScore(900));
        assertEquals(ObdxRank.S, ObdxRank.fromScore(901));
        assertEquals(ObdxRank.S, ObdxRank.fromScore(1000));
    }

    @Test
    void label_from_score_is_the_bare_letter() {
        assertEquals("E", ObdxRank.labelFromScore(125));     // COBS club trial
        assertEquals("D", ObdxRank.labelFromScore(251));     // FCI grade 1 club trial
        assertEquals("C", ObdxRank.labelFromScore(451));     // FCI grade 2 club trial
        assertEquals("B", ObdxRank.labelFromScore(700));     // FCI grade 3 club trial, tier 3
        assertEquals("A", ObdxRank.labelFromScore(900));     // world championship semi-final
    }

    @Test
    void label_from_score_reports_S_for_a_world_championship_final() {
        assertEquals("S", ObdxRank.labelFromScore(901));
        assertEquals("S", ObdxRank.labelFromScore(1000));
    }
}
