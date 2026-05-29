package com.k9x.application.events.obdx.use_case.dto;

import java.math.BigDecimal;
import java.util.List;

public record FetchClassificationExerciseScoreDTO(
        String exerciseId, short exercisePosition, List<String> tags,
        BigDecimal rawScore, BigDecimal totalScore, BigDecimal scoreRating,
        List<FetchClassificationJudgeScoreDTO> judgeScores) {
}
