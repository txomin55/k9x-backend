package com.k9x.application.stages.use_case.dto;

import com.k9x.domain.competitions.aggregates.CompetitionExtraction;

import java.util.List;

/**
 * Query projection behind the public stage list: one active stage with the few competition fields the list
 * shows and its events. It is read straight from the tables, not from the competition aggregate, so the list
 * never loads what it does not show.
 */
public record FetchStageListRowDTO(String id, String name, long dateFrom, long dateTo,
                                   String competitionName, String country, String address,
                                   Double coordAlt, Double coordLong, String organizer,
                                   CompetitionExtraction extraction, List<FetchStageListRowEventDTO> events) {

    public FetchStageListRowDTO {
        events = events == null ? List.of() : events;
    }
}
