package com.k9x.application.competitions.use_case.dto;

import com.k9x.domain.competitions.aggregates.CompetitionSource;

import java.util.List;

public record FetchCompetitionDTO(String id, String name, String description, String country, String address,
                                  String status, CompetitionSource source, List<FetchStageDTO> stages) {
}
