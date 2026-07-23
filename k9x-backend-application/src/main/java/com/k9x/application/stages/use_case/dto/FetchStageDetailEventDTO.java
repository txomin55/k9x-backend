package com.k9x.application.stages.use_case.dto;

import java.util.List;

public record FetchStageDetailEventDTO(String id, String name, String disciplineId,
                                       String configurationId, String configurationName,
                                       List<FetchStageDetailCompetitorDTO> competitors, String status,
                                       boolean enrollmentOpened, Long enrollmentDeadline, List<String> awards,
                                       String rank) {

    public FetchStageDetailEventDTO {
        awards = awards == null ? List.of() : awards;
    }
}
