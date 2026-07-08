package com.k9x.application.stages.use_case.dto;

import java.util.List;

public record FetchStageListEventDTO(String id, String name, String disciplineId, int competitorCount, String status,
                                     boolean enrollmentOpened, Long enrollmentDeadline, List<String> awards,
                                     String rank) {
}
