package com.k9x.application.collections.use_case.dto;

import java.math.BigDecimal;
import java.util.List;

public record FetchCollectionDetailDTO(
    String configurationId,
    List<BigDecimal> allowedValues,
    List<FetchCollectionJudgeWithCollectorDTO> judges,
    List<FetchCollectionCompetitorDTO> competitors,
    List<FetchCollectionExerciseDTO> exercises,
    List<FetchCollectionScoreDTO> scores
) {}
