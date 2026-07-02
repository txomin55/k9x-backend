package com.k9x.application.events.obdx.use_case.dto;

public record FetchClassificationDTO(
        String eventId, String eventName, String eventStatus,
        String stageId, String stageName,
        String disciplineId,
        String configurationId, String configurationName,
        Long scoresLastUpdate,
        FetchObdxClassificationDTO obdx) {
}
