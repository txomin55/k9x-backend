package com.k9x.domain.competitions.aggregates;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CompetitionSnapshotExtractionTest {

    private static final CompetitionExtraction METADATA =
            new CompetitionExtraction("cpc-2020-9-extraction", "https://cpc/2020/9", 1000L, "FEDERATION_PAGE,cpc");

    private static CompetitionSnapshot competition(CompetitionSource source, CompetitionExtraction metadata) {
        return new CompetitionSnapshot("comp-1", "World Cup", "creator", "Org", "ES", "desc", "addr",
                null, null, source, metadata, 0L, 0L, null, List.of());
    }

    @Test
    void has_no_extraction_when_the_competition_was_created_through_the_app() {
        assertNull(competition(CompetitionSource.API, null).extraction());
        assertNull(competition(CompetitionSource.API, METADATA).extraction());
    }

    @Test
    void exposes_the_metadata_of_an_extracted_competition() {
        assertEquals(METADATA, competition(CompetitionSource.EXTRACTION, METADATA).extraction());
    }

    @Test
    void warns_about_an_extracted_competition_even_when_nobody_wrote_down_where_it_came_from() {
        assertEquals(CompetitionExtraction.UNKNOWN, competition(CompetitionSource.EXTRACTION, null).extraction());
    }
}
