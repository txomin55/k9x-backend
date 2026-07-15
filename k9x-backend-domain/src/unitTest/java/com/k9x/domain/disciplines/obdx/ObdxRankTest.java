package com.k9x.domain.disciplines.obdx;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ObdxRankTest {

    @Test
    void resolves_letter_from_competitor_count() {
        assertEquals(ObdxRank.E, ObdxRank.fromCompetitorCount(0));
        assertEquals(ObdxRank.E, ObdxRank.fromCompetitorCount(4));
        assertEquals(ObdxRank.D, ObdxRank.fromCompetitorCount(5));
        assertEquals(ObdxRank.D, ObdxRank.fromCompetitorCount(9));
        assertEquals(ObdxRank.C, ObdxRank.fromCompetitorCount(10));
        assertEquals(ObdxRank.C, ObdxRank.fromCompetitorCount(19));
        assertEquals(ObdxRank.B, ObdxRank.fromCompetitorCount(20));
        assertEquals(ObdxRank.B, ObdxRank.fromCompetitorCount(39));
        assertEquals(ObdxRank.A, ObdxRank.fromCompetitorCount(40));
        assertEquals(ObdxRank.A, ObdxRank.fromCompetitorCount(100));
    }

    @Test
    void format_appends_plus_only_when_international() {
        assertEquals("B", ObdxRank.B.format(false));
        assertEquals("B+", ObdxRank.B.format(true));
    }
}
