package com.k9x.application.stages.use_case.dto;

import com.k9x.domain.competitions.aggregates.CompetitionExtraction;

import java.util.List;

/**
 * Query projection behind the public stage detail: the stage, deleted or not (the service case tells the two
 * apart), the few competition fields it shows and every event of the stage. Read straight from the tables, not
 * from the competition aggregate, so the sibling stages and the scores are never loaded.
 */
public record FetchStageDetailRowDTO(String id, String name, long dateFrom, long dateTo, Long deletedAt,
                                     String competitionName, String address, String organizer,
                                     CompetitionExtraction extraction, List<FetchStageDetailRowEventDTO> events) {

    public FetchStageDetailRowDTO {
        events = events == null ? List.of() : events;
    }
}
