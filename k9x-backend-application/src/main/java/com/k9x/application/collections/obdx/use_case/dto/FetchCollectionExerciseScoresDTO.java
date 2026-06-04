package com.k9x.application.collections.obdx.use_case.dto;

import java.util.List;

public record FetchCollectionExerciseScoresDTO(
        String exerciseId, Short position,
        List<FetchCollectionJudgeScoreDTO> scores) {}
