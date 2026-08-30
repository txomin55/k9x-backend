package com.k9x.domain.competitions.aggregates;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompetitionExtractionTest {

    private static CompetitionExtraction ofType(String type) {
        return new CompetitionExtraction("id", "https://source", 1L, type);
    }

    @Test
    void reads_the_type_as_the_first_token_and_the_rest_as_parameters() {
        assertEquals("FEDERATION_PAGE", ofType("FEDERATION_PAGE,cpc").typeToken());
        assertEquals(List.of("cpc"), ofType("FEDERATION_PAGE,cpc").typeParams());
        assertEquals(List.of("ORGANIZER", "2020"), ofType("PRIVATE_CONVERSATIONS,ORGANIZER,2020").typeParams());
    }

    @Test
    void reads_a_type_with_no_parameters() {
        assertEquals("FEDERATION_PAGE", ofType("FEDERATION_PAGE").typeToken());
        assertTrue(ofType("FEDERATION_PAGE").typeParams().isEmpty());
    }

    @Test
    void has_no_type_when_it_was_not_stored() {
        assertNull(ofType(null).typeToken());
        assertNull(ofType("  ").typeToken());
        assertTrue(ofType(null).typeParams().isEmpty());
    }
}
