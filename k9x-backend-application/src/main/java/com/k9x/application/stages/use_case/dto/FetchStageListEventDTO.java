package com.k9x.application.stages.use_case.dto;

public record FetchStageListEventDTO(String id, String name, String configurationId, String disciplineName, int competitorCount, String status) {
}
