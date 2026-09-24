package com.k9x.application.extractions.use_case.dto;

/** One active event of a {@link FetchExtractionLogStageDTO}; {@code competitorCount} is aggregated in SQL. */
public record FetchExtractionLogEventDTO(String id, String name, String disciplineId, int competitorCount,
                                         Integer rankScore) {
}
