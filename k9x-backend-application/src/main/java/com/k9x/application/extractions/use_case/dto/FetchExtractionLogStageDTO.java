package com.k9x.application.extractions.use_case.dto;

import java.util.List;

/**
 * Query projection behind the extraction log: one active stage of an active extracted competition, with the
 * instant its latest extraction was loaded and its active events. Read straight from the tables, so the log
 * never loads competitors or scores to show a count.
 */
public record FetchExtractionLogStageDTO(String id, String name, String competitionName, String country,
                                         long dateFrom, long dateTo, long loadedAt,
                                         List<FetchExtractionLogEventDTO> events) {

    public FetchExtractionLogStageDTO {
        events = events == null ? List.of() : events;
    }
}
