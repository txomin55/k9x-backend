package com.k9x.application.events.obdx.use_case.dto;

public record FetchClassificationDTO(
        String eventId, String eventName,
        String stageId, String stageName,
        String configurationId,
        Long scoresLastUpdate,
        FetchObdxClassificationDTO obdx) {
}
