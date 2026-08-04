package com.k9x.domain.disciplines.obdx;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObdxEventRankTest {

    @Test
    void foreign_requires_a_country_that_differs_from_the_event_country() {
        assertTrue(ObdxEventRank.isForeign("FR", "ES"));
        assertFalse(ObdxEventRank.isForeign("es", "ES"));   // case-insensitive
        assertFalse(ObdxEventRank.isForeign(null, "ES"));
        assertFalse(ObdxEventRank.isForeign("  ", "ES"));
    }

    @Test
    void required_foreign_competitors_grows_with_the_competitor_tier() {
        assertEquals(1, ObdxEventRank.requiredForeignCompetitors(0));    // tier 1
        assertEquals(1, ObdxEventRank.requiredForeignCompetitors(4));    // tier 1
        assertEquals(2, ObdxEventRank.requiredForeignCompetitors(5));    // tier 2
        assertEquals(2, ObdxEventRank.requiredForeignCompetitors(9));    // tier 2
        assertEquals(2, ObdxEventRank.requiredForeignCompetitors(10));   // tier 3
        assertEquals(2, ObdxEventRank.requiredForeignCompetitors(19));   // tier 3
        assertEquals(3, ObdxEventRank.requiredForeignCompetitors(20));   // tier 4
        assertEquals(3, ObdxEventRank.requiredForeignCompetitors(34));   // tier 4
        assertEquals(4, ObdxEventRank.requiredForeignCompetitors(35));   // tier 5
        assertEquals(4, ObdxEventRank.requiredForeignCompetitors(200));  // tier 5
    }

    @Test
    void small_events_stay_international_with_a_single_foreign_competitor() {
        assertTrue(ObdxEventRank.isInternational(List.of("ES", "ES", "FR"), "ES"));
        assertFalse(ObdxEventRank.isInternational(List.of("ES", "ES", "ES"), "ES"));
    }

    @Test
    void bigger_events_need_more_than_one_foreign_competitor() {
        // 6 competitors (tier 2) need 2 foreigners.
        assertFalse(ObdxEventRank.isInternational(countries(5, "ES", 1, "FR"), "ES"));
        assertTrue(ObdxEventRank.isInternational(countries(4, "ES", 2, "FR"), "ES"));

        // 25 competitors (tier 4) need 3 foreigners.
        assertFalse(ObdxEventRank.isInternational(countries(23, "ES", 2, "FR"), "ES"));
        assertTrue(ObdxEventRank.isInternational(countries(22, "ES", 3, "FR"), "ES"));

        // 40 competitors (tier 5) need 4 foreigners.
        assertFalse(ObdxEventRank.isInternational(countries(37, "ES", 3, "FR"), "ES"));
        assertTrue(ObdxEventRank.isInternational(countries(36, "ES", 4, "FR"), "ES"));
    }

    @Test
    void competitors_with_no_country_still_count_towards_the_event_size() {
        // 12 competitors (tier 3, needs 2 foreigners): the unknown countries are not foreign but do count as
        // competitors, so a single FR is not enough.
        List<String> countries = new ArrayList<>(countries(1, "ES", 1, "FR"));
        countries.addAll(Arrays.asList(new String[10]));
        assertFalse(ObdxEventRank.isInternational(countries, "ES"));
    }

    @Test
    void event_score_is_null_when_the_configuration_has_no_band() {
        assertEquals(128, ObdxEventRank.eventScore("OBDX.CPC_COBS.V0", 3, true));
        assertNull(ObdxEventRank.eventScore("UNKNOWN.V0", 3, true));
    }

    private static List<String> countries(int localCount, String local, int foreignCount, String foreign) {
        List<String> countries = new ArrayList<>(Collections.nCopies(localCount, local));
        countries.addAll(Collections.nCopies(foreignCount, foreign));
        return countries;
    }
}
