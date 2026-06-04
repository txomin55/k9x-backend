package com.k9x.application.events.obdx.use_case.dto;

import java.math.BigDecimal;
import java.util.List;

public record FetchClassificationCompetitorDTO(
        String dogId, String dogName, String owner, String team, String country,
        int position, BigDecimal totalScore, BigDecimal scoreRating, boolean tied, String status,
        List<FetchClassificationExerciseScoreDTO> exercises) {
}
