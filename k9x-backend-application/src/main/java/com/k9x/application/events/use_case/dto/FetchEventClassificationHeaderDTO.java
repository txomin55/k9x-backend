package com.k9x.application.events.use_case.dto;

import com.k9x.domain.competitions.aggregates.CompetitionExtraction;

/**
 * Query projection of what a classification shows around its results: the event, deleted or not (the service
 * case tells the two apart), its stage and its competition. {@code hasAnyScore} is aggregated in SQL, so the
 * header answers the lifecycle rules without loading a score.
 */
public record FetchEventClassificationHeaderDTO(String eventId, String name, String disciplineId,
                                                String configurationId, Long deletedAt, Integer rankScore,
                                                boolean hasAnyScore, String stageId, String stageName,
                                                long stageDateTo, String competitionName,
                                                CompetitionExtraction competitionExtraction) {
}
