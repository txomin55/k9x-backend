package com.k9x.application.stages.use_case.dto;

import java.util.List;

/**
 * One event of a {@link FetchStageDetailRowDTO}, deleted ones included: the stage status needs them, the detail
 * drops them. It carries its competitors and the facts the lifecycle rules need instead of the scores:
 * {@code hasAnyScore} is aggregated in SQL, and {@code allCompetitorsSettled} is only worked out while the stage
 * has not finished by date.
 */
public record FetchStageDetailRowEventDTO(String id, String name, String disciplineId, String configurationId,
                                          Long deletedAt, Long enrollmentDeadline, List<String> awards,
                                          Integer rankScore, List<FetchStageDetailCompetitorDTO> competitors,
                                          boolean hasAnyScore, boolean allCompetitorsSettled) {

    public FetchStageDetailRowEventDTO {
        awards = awards == null ? List.of() : awards;
        competitors = competitors == null ? List.of() : competitors;
    }
}
