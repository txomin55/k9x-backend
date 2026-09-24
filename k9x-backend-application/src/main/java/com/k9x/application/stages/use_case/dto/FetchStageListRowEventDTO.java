package com.k9x.application.stages.use_case.dto;

import java.util.List;

/**
 * One event of a {@link FetchStageListRowDTO}, deleted ones included: the stage status needs them, the list
 * drops them. It carries the facts the lifecycle rules need instead of the scores themselves:
 * {@code hasAnyScore} and {@code competitorCount} are aggregated in SQL, and {@code allCompetitorsSettled} is
 * only worked out for events whose stage has not finished by date — for the rest the date alone decides.
 */
public record FetchStageListRowEventDTO(String id, String name, String disciplineId, Long deletedAt,
                                        Long enrollmentDeadline, List<String> awards, Integer rankScore,
                                        int competitorCount, boolean hasAnyScore, boolean allCompetitorsSettled) {

    public FetchStageListRowEventDTO {
        awards = awards == null ? List.of() : awards;
    }
}
