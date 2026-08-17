package com.k9x.domain.disciplines.obdx;

import org.junit.jupiter.api.Test;

import static com.k9x.domain.disciplines.obdx.ObdxEventCategory.CLUB;
import static com.k9x.domain.disciplines.obdx.ObdxEventCategory.OPEN;
import static com.k9x.domain.disciplines.obdx.ObdxEventCategory.WC_FINAL;
import static com.k9x.domain.disciplines.obdx.ObdxEventCategory.WC_Q;
import static com.k9x.domain.disciplines.obdx.ObdxEventCategory.WC_SEMI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObdxConfigurationsRankThresholdsTest {

    // Representative competitor counts for each tier: <10, [10,25), >=25.
    private static final int TIER_1 = 3;
    private static final int TIER_2 = 10;
    private static final int TIER_3 = 25;

    @Test
    void resolves_band_from_configuration_id_ignoring_the_version_suffix() {
        assertEquals(ObdxConfigurationsRankThresholds.FCI_GRADE_3,
                ObdxConfigurationsRankThresholds.fromConfigurationId("OBDX.FCI_GRADE_3.V0"));
        assertEquals(ObdxConfigurationsRankThresholds.FCI_GRADE_3,
                ObdxConfigurationsRankThresholds.fromConfigurationId("OBDX.FCI_GRADE_3.V7"));
        assertEquals(ObdxConfigurationsRankThresholds.CPC_COBS,
                ObdxConfigurationsRankThresholds.fromConfigurationId("OBDX.CPC_COBS"));
        assertNull(ObdxConfigurationsRankThresholds.fromConfigurationId("UNKNOWN.V0"));
        assertNull(ObdxConfigurationsRankThresholds.fromConfigurationId(null));
    }

    @Test
    void tier_from_competitor_count_buckets_by_thresholds() {
        assertEquals(1, ObdxConfigurationsRankThresholds.tierFromCompetitorCount(0));
        assertEquals(1, ObdxConfigurationsRankThresholds.tierFromCompetitorCount(9));
        assertEquals(2, ObdxConfigurationsRankThresholds.tierFromCompetitorCount(10));
        assertEquals(2, ObdxConfigurationsRankThresholds.tierFromCompetitorCount(24));
        assertEquals(3, ObdxConfigurationsRankThresholds.tierFromCompetitorCount(25));
        assertEquals(3, ObdxConfigurationsRankThresholds.tierFromCompetitorCount(100));
    }

    @Test
    void only_the_world_championship_grade_admits_the_wc_categories() {
        assertTrue(ObdxConfigurationsRankThresholds.FCI_GRADE_3.allows(CLUB));
        assertTrue(ObdxConfigurationsRankThresholds.FCI_GRADE_3.allows(OPEN));
        assertTrue(ObdxConfigurationsRankThresholds.FCI_GRADE_3.allows(WC_Q));
        assertTrue(ObdxConfigurationsRankThresholds.FCI_GRADE_3.allows(WC_SEMI));
        assertTrue(ObdxConfigurationsRankThresholds.FCI_GRADE_3.allows(WC_FINAL));

        assertTrue(ObdxConfigurationsRankThresholds.FCI_GRADE_2.allows(CLUB));
        assertTrue(ObdxConfigurationsRankThresholds.FCI_GRADE_2.allows(OPEN));
        assertFalse(ObdxConfigurationsRankThresholds.FCI_GRADE_2.allows(WC_Q));
        assertFalse(ObdxConfigurationsRankThresholds.FCI_GRADE_2.allows(WC_FINAL));
        assertFalse(ObdxConfigurationsRankThresholds.CPC_COBS.allows(WC_SEMI));
        assertFalse(ObdxConfigurationsRankThresholds.FCI_GRADE_1.allows(null));
    }

    @Test
    void scoring_a_category_the_configuration_does_not_admit_is_a_programming_error() {
        assertThrows(IllegalArgumentException.class,
                () -> ObdxConfigurationsRankThresholds.FCI_GRADE_2.eventScore(TIER_1, WC_FINAL));
    }

    @Test
    void club_takes_the_lower_three_quarters_of_a_band_and_open_the_top_quarter() {
        assertEquals(new ObdxConfigurationsRankThresholds.Band(50, 88),
                ObdxConfigurationsRankThresholds.ENCI_PRE_DEBUTTANTI.subBand(CLUB));
        assertEquals(new ObdxConfigurationsRankThresholds.Band(89, 100),
                ObdxConfigurationsRankThresholds.ENCI_PRE_DEBUTTANTI.subBand(OPEN));
        assertEquals(new ObdxConfigurationsRankThresholds.Band(100, 175),
                ObdxConfigurationsRankThresholds.CPC_COBS.subBand(CLUB));
        assertEquals(new ObdxConfigurationsRankThresholds.Band(176, 200),
                ObdxConfigurationsRankThresholds.CPC_COBS.subBand(OPEN));
        assertEquals(new ObdxConfigurationsRankThresholds.Band(201, 350),
                ObdxConfigurationsRankThresholds.FCI_GRADE_1.subBand(CLUB));
        assertEquals(new ObdxConfigurationsRankThresholds.Band(351, 400),
                ObdxConfigurationsRankThresholds.FCI_GRADE_1.subBand(OPEN));
        assertEquals(new ObdxConfigurationsRankThresholds.Band(401, 550),
                ObdxConfigurationsRankThresholds.FCI_GRADE_2.subBand(CLUB));
        assertEquals(new ObdxConfigurationsRankThresholds.Band(551, 600),
                ObdxConfigurationsRankThresholds.FCI_GRADE_2.subBand(OPEN));
    }

    @Test
    void grade_3_splits_into_club_open_a_qualifier_band_and_two_fixed_championship_points() {
        assertEquals(new ObdxConfigurationsRankThresholds.Band(601, 700),
                ObdxConfigurationsRankThresholds.FCI_GRADE_3.subBand(CLUB));
        assertEquals(new ObdxConfigurationsRankThresholds.Band(701, 750),
                ObdxConfigurationsRankThresholds.FCI_GRADE_3.subBand(OPEN));
        assertEquals(new ObdxConfigurationsRankThresholds.Band(775, 850),
                ObdxConfigurationsRankThresholds.FCI_GRADE_3.subBand(WC_Q));
        assertEquals(new ObdxConfigurationsRankThresholds.Band(900, 900),
                ObdxConfigurationsRankThresholds.FCI_GRADE_3.subBand(WC_SEMI));
        assertEquals(new ObdxConfigurationsRankThresholds.Band(1000, 1000),
                ObdxConfigurationsRankThresholds.FCI_GRADE_3.subBand(WC_FINAL));
    }

    @Test
    void event_score_walks_the_sub_band_by_tier() {
        assertBaremo(ObdxConfigurationsRankThresholds.ENCI_PRE_DEBUTTANTI, CLUB, 63, 75, 88);
        assertBaremo(ObdxConfigurationsRankThresholds.ENCI_PRE_DEBUTTANTI, OPEN, 93, 96, 100);
        assertBaremo(ObdxConfigurationsRankThresholds.ENCI_DEBUTTANTI, CLUB, 125, 150, 175);
        assertBaremo(ObdxConfigurationsRankThresholds.ENCI_DEBUTTANTI, OPEN, 184, 192, 200);
        assertBaremo(ObdxConfigurationsRankThresholds.RSCE_DEBUTANTE, CLUB, 125, 150, 175);
        assertBaremo(ObdxConfigurationsRankThresholds.CPC_COBS, CLUB, 125, 150, 175);
        assertBaremo(ObdxConfigurationsRankThresholds.CPC_COBS, OPEN, 184, 192, 200);
        assertBaremo(ObdxConfigurationsRankThresholds.FCI_GRADE_1, CLUB, 251, 300, 350);
        assertBaremo(ObdxConfigurationsRankThresholds.FCI_GRADE_1, OPEN, 367, 384, 400);
        assertBaremo(ObdxConfigurationsRankThresholds.RSCE_GRADO_1, CLUB, 251, 300, 350);
        assertBaremo(ObdxConfigurationsRankThresholds.FCI_GRADE_2, CLUB, 451, 500, 550);
        assertBaremo(ObdxConfigurationsRankThresholds.FCI_GRADE_2, OPEN, 567, 584, 600);
        assertBaremo(ObdxConfigurationsRankThresholds.FCI_GRADE_3, CLUB, 634, 667, 700);
        assertBaremo(ObdxConfigurationsRankThresholds.FCI_GRADE_3, OPEN, 717, 734, 750);
        assertBaremo(ObdxConfigurationsRankThresholds.FCI_GRADE_3, WC_Q, 800, 825, 850);
    }

    @Test
    void the_semi_final_and_the_final_are_worth_the_same_however_many_competitors_turn_up() {
        assertBaremo(ObdxConfigurationsRankThresholds.FCI_GRADE_3, WC_SEMI, 900, 900, 900);
        assertBaremo(ObdxConfigurationsRankThresholds.FCI_GRADE_3, WC_FINAL, 1000, 1000, 1000);
    }

    @Test
    void the_letter_follows_the_grade_and_the_final_reaches_S() {
        assertEquals(ObdxRank.B, ObdxRank.fromScore(
                ObdxConfigurationsRankThresholds.FCI_GRADE_3.eventScore(TIER_1, CLUB)));
        // The qualifier straddles the B/A border: 800 is still B, a crowded one (825+) already reaches A.
        assertEquals(ObdxRank.B, ObdxRank.fromScore(
                ObdxConfigurationsRankThresholds.FCI_GRADE_3.eventScore(TIER_1, WC_Q)));
        assertEquals(ObdxRank.A, ObdxRank.fromScore(
                ObdxConfigurationsRankThresholds.FCI_GRADE_3.eventScore(TIER_3, WC_Q)));
        assertEquals(ObdxRank.A, ObdxRank.fromScore(
                ObdxConfigurationsRankThresholds.FCI_GRADE_3.eventScore(TIER_1, WC_SEMI)));
        assertEquals(ObdxRank.S, ObdxRank.fromScore(
                ObdxConfigurationsRankThresholds.FCI_GRADE_3.eventScore(TIER_1, WC_FINAL)));
        assertEquals(ObdxRank.C, ObdxRank.fromScore(
                ObdxConfigurationsRankThresholds.FCI_GRADE_2.eventScore(TIER_3, OPEN)));
        assertEquals(ObdxRank.E, ObdxRank.fromScore(
                ObdxConfigurationsRankThresholds.CPC_COBS.eventScore(TIER_3, OPEN)));
    }

    /**
     * The tier never lands on the sub-band floor: an event sitting exactly on it would leave every qualified
     * competitor tied, because that floor is also what a competitor's own score is measured against.
     */
    @Test
    void the_smallest_event_still_scores_above_its_sub_band_floor() {
        for (ObdxConfigurationsRankThresholds band : ObdxConfigurationsRankThresholds.values()) {
            for (ObdxEventCategory category : ObdxEventCategory.values()) {
                if (!band.allows(category)) {
                    continue;
                }
                ObdxConfigurationsRankThresholds.Band subBand = band.subBand(category);
                if (subBand.min() == subBand.max()) {
                    continue;   // championship rounds are single points by design
                }
                assertTrue(band.eventScore(0, category) > subBand.min(),
                        band + "/" + category + " tier 1 must sit above its floor " + subBand.min());
            }
        }
    }

    private void assertBaremo(ObdxConfigurationsRankThresholds band, ObdxEventCategory category,
                              int tier1, int tier2, int tier3) {
        assertEquals(tier1, band.eventScore(TIER_1, category), band + "/" + category + " tier 1");
        assertEquals(tier2, band.eventScore(TIER_2, category), band + "/" + category + " tier 2");
        assertEquals(tier3, band.eventScore(TIER_3, category), band + "/" + category + " tier 3");
    }
}
