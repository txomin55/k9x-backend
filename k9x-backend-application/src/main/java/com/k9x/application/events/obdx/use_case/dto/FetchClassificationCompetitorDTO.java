package com.k9x.application.events.obdx.use_case.dto;

import java.math.BigDecimal;
import java.util.List;

public record FetchClassificationCompetitorDTO(
        String dogId, String dogName, String breed, String owner, String handler, String team, String country,
        Short startOrder, Short competitorNumber, int position, BigDecimal totalScore, BigDecimal scoreRating,
        boolean tied, String status,
        Boolean bih, Boolean reserve, boolean notCompeting, List<FetchClassificationExerciseScoreDTO> exercises,
        List<String> awards, String qualification, BigDecimal rankScore) {
}
