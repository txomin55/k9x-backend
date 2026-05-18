package com.k9x.application.competitions.dto;

import java.util.List;

public record FetchCompetitionDTO(String id, String name, String description, String country, String address, String status, List<FetchStageDTO> stages) {
}
