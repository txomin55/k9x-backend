package com.k9x.domain.disciplines.obdx;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ObdxConfigurationsRankThresholdsTest {

    @Test
    void resolves_band_from_configuration_id_ignoring_the_version_suffix() {
        assertEquals(ObdxConfigurationsRankThresholds.FCI_GRADE_3,
                ObdxConfigurationsRankThresholds.fromConfigurationId("OBDX_FCI_GRADE_3.V0"));
        assertEquals(ObdxConfigurationsRankThresholds.FCI_GRADE_3,
                ObdxConfigurationsRankThresholds.fromConfigurationId("OBDX_FCI_GRADE_3.V7"));
        assertEquals(ObdxConfigurationsRankThresholds.CPC_COBS,
                ObdxConfigurationsRankThresholds.fromConfigurationId("CPC_COBS"));
        assertNull(ObdxConfigurationsRankThresholds.fromConfigurationId("UNKNOWN.V0"));
        assertNull(ObdxConfigurationsRankThresholds.fromConfigurationId(null));
    }

    @Test
    void tier_from_competitor_count_buckets_by_thresholds() {
        assertEquals(1, ObdxConfigurationsRankThresholds.tierFromCompetitorCount(0));
        assertEquals(1, ObdxConfigurationsRankThresholds.tierFromCompetitorCount(4));
        assertEquals(2, ObdxConfigurationsRankThresholds.tierFromCompetitorCount(5));
        assertEquals(3, ObdxConfigurationsRankThresholds.tierFromCompetitorCount(10));
        assertEquals(4, ObdxConfigurationsRankThresholds.tierFromCompetitorCount(20));
        assertEquals(5, ObdxConfigurationsRankThresholds.tierFromCompetitorCount(35));
        assertEquals(5, ObdxConfigurationsRankThresholds.tierFromCompetitorCount(100));
    }

    @Test
    void event_score_places_the_event_within_the_configuration_band() {
        // CPC_COBS band [100, 200], range 100: tier step = 18, international bonus = 10.
        // The worked example: 3 competitors (tier 1) + international = 100 + 18 + 10 = 128.
        assertEquals(128, ObdxConfigurationsRankThresholds.CPC_COBS.eventScore(3, true));
        assertEquals(118, ObdxConfigurationsRankThresholds.CPC_COBS.eventScore(3, false));
        assertEquals(200, ObdxConfigurationsRankThresholds.CPC_COBS.eventScore(40, true));   // tier 5 + intl tops the band
    }

    @Test
    void event_score_never_reaches_the_manual_S_range() {
        // FCI_GRADE_3 band [601, 900]: the maximum automatic score (tier 5 + international) is exactly 900,
        // so the automatic formula never reaches the S range (901–1000).
        assertEquals(900, ObdxConfigurationsRankThresholds.FCI_GRADE_3.eventScore(40, true));
        assertEquals(ObdxRank.A, ObdxRank.fromScore(ObdxConfigurationsRankThresholds.FCI_GRADE_3.eventScore(40, true)));
    }
}
