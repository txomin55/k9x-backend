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
        assertEquals(ObdxRank.B, ObdxRank.fromCompetitorCount(34));
        assertEquals(ObdxRank.A, ObdxRank.fromCompetitorCount(35));
        assertEquals(ObdxRank.A, ObdxRank.fromCompetitorCount(100));
    }

    @Test
    void format_appends_plus_only_when_international() {
        assertEquals("B", ObdxRank.B.format(false));
        assertEquals("B+", ObdxRank.B.format(true));
    }

    @Test
    void tier_weights_go_from_one_at_E_to_five_at_A() {
        assertEquals(1, ObdxRank.E.tier());
        assertEquals(2, ObdxRank.D.tier());
        assertEquals(3, ObdxRank.C.tier());
        assertEquals(4, ObdxRank.B.tier());
        assertEquals(5, ObdxRank.A.tier());
    }

    @Test
    void score_places_the_rank_within_the_configuration_band() {
        // CPC_COBS band [100, 200], range 100: tier step = 18, international bonus = 10.
        assertEquals(118, ObdxRank.E.score(100, 200, false));
        assertEquals(128, ObdxRank.E.score(100, 200, true));   // the worked example
        assertEquals(190, ObdxRank.A.score(100, 200, false));
        assertEquals(200, ObdxRank.A.score(100, 200, true));   // A + international tops the band
    }

    @Test
    void score_never_exceeds_the_automatic_ceiling_for_the_top_band() {
        // FCI_GRADE_3 band [601, 950]: A + international must land exactly on the 950 ceiling.
        assertEquals(950, ObdxRank.A.score(601, 950, true));
        assertEquals(950, ObdxRank.MAX_AUTOMATIC_SCORE);
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
        assertEquals(ObdxRank.A, ObdxRank.fromScore(950));
    }

    @Test
    void label_from_score_combines_the_global_letter_with_the_international_suffix() {
        assertEquals("E", ObdxRank.labelFromScore(136, false));     // COBS, national
        assertEquals("D", ObdxRank.labelFromScore(237, false));     // FCI grade 1, national
        assertEquals("C", ObdxRank.labelFromScore(437, false));     // FCI grade 2, national
        assertEquals("B+", ObdxRank.labelFromScore(699, true));     // FCI grade 3, international
        assertEquals("A+", ObdxRank.labelFromScore(950, true));     // top of the automatic scale, international
    }

    @Test
    void label_from_score_reports_A_plus_plus_above_the_automatic_ceiling() {
        assertEquals(ObdxRank.EXCEPTIONAL, ObdxRank.labelFromScore(975, false));
        assertEquals("A++", ObdxRank.labelFromScore(1000, true));
    }
}
