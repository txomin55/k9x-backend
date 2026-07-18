package com.k9x.domain.disciplines.obdx;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ObdxRankTest {

    @Test
    void format_appends_plus_only_when_international() {
        assertEquals("B", ObdxRank.B.format(false));
        assertEquals("B+", ObdxRank.B.format(true));
        assertEquals("S+", ObdxRank.S.format(true));   // S is always international
    }

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
    void range_floor_returns_the_lower_bound_of_each_global_band() {
        assertEquals(0, ObdxRank.E.rangeFloor());
        assertEquals(201, ObdxRank.D.rangeFloor());
        assertEquals(401, ObdxRank.C.rangeFloor());
        assertEquals(601, ObdxRank.B.rangeFloor());
        assertEquals(801, ObdxRank.A.rangeFloor());
        assertEquals(901, ObdxRank.S.rangeFloor());
    }

    @Test
    void label_from_score_combines_the_global_letter_with_the_international_suffix() {
        assertEquals("E", ObdxRank.labelFromScore(136, false));     // COBS, national
        assertEquals("D", ObdxRank.labelFromScore(237, false));     // FCI grade 1, national
        assertEquals("C", ObdxRank.labelFromScore(437, false));     // FCI grade 2, national
        assertEquals("B+", ObdxRank.labelFromScore(699, true));     // FCI grade 3, international
        assertEquals("A+", ObdxRank.labelFromScore(900, true));     // top of the automatic scale, international
    }

    @Test
    void label_from_score_reports_S_in_the_manual_range() {
        assertEquals("S", ObdxRank.labelFromScore(901, false));
        assertEquals("S+", ObdxRank.labelFromScore(1000, true));    // S seeds are always international
    }
}
