package com.k9x.application.competitions.use_case.dto;

import com.k9x.domain.competitions.aggregates.CompetitionExtraction;

import java.util.List;

public record FetchCompetitionDTO(String id, String name, String description, String country, String address,
                                  String status, CompetitionExtraction extraction, List<FetchStageDTO> stages) {
}
