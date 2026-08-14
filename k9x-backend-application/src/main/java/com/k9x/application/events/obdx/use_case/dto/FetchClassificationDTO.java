package com.k9x.application.events.obdx.use_case.dto;

import com.k9x.domain.competitions.aggregates.CompetitionSource;

public record FetchClassificationDTO(
        String eventId, String eventName, String eventStatus,
        String stageId, String stageName,
        String competitionName,
        String disciplineId,
        String configurationId, String configurationName,
        Long scoresLastUpdate,
        FetchObdxClassificationDTO obdx,
        String rank,
        CompetitionSource source) {
}
