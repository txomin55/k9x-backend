package com.k9x.application.collections.obdx.use_case.dto;

import com.k9x.application.collections.use_case.dto.FetchCollectionCompetitorDTO;

import java.util.List;

public record FetchCollectionCompetitorScoresDTO(
        FetchCollectionCompetitorDTO competitor,
        List<FetchCollectionExerciseScoresDTO> exercises) {}
