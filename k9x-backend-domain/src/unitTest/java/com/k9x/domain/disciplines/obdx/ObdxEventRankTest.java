package com.k9x.domain.disciplines.obdx;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObdxEventRankTest {

    @Test
    void the_wc_categories_are_allowed_only_for_the_championship_grade() {
        assertTrue(ObdxEventRank.isCategoryAllowed("OBDX.FCI_GRADE_3.V0", ObdxEventCategory.WC_FINAL));
        assertTrue(ObdxEventRank.isCategoryAllowed("OBDX.FCI_GRADE_3.V0", ObdxEventCategory.CLUB));
        assertFalse(ObdxEventRank.isCategoryAllowed("OBDX.FCI_GRADE_2.V0", ObdxEventCategory.WC_FINAL));
        assertTrue(ObdxEventRank.isCategoryAllowed("OBDX.FCI_GRADE_2.V0", ObdxEventCategory.OPEN));
    }

    @Test
    void an_unknown_configuration_or_a_missing_category_allows_nothing() {
        assertFalse(ObdxEventRank.isCategoryAllowed("UNKNOWN.V0", ObdxEventCategory.CLUB));
        assertFalse(ObdxEventRank.isCategoryAllowed("OBDX.FCI_GRADE_3.V0", null));
    }

    @Test
    void event_score_places_the_event_in_its_category_sub_band() {
        assertEquals(125, ObdxEventRank.eventScore("OBDX.CPC_COBS.V0", 3, ObdxEventCategory.CLUB));
        assertEquals(184, ObdxEventRank.eventScore("OBDX.CPC_COBS.V0", 3, ObdxEventCategory.OPEN));
        assertEquals(1000, ObdxEventRank.eventScore("OBDX.FCI_GRADE_3.V0", 3, ObdxEventCategory.WC_FINAL));
    }

    @Test
    void event_score_is_null_when_the_configuration_has_no_band() {
        assertNull(ObdxEventRank.eventScore("UNKNOWN.V0", 3, ObdxEventCategory.CLUB));
    }
}
