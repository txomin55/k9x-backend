package com.k9x.application.events.obdx.use_cases.dto;

import com.k9x.domain.aggregates.disciplines.ClassificationCacheEvictStrategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ObdxClassificationConfigDTO(
        ClassificationCacheEvictStrategy cacheEvictStrategy,
        BigDecimal maxAllowedScore,
        Map<String, BigDecimal> coefByExerciseId,
        List<String> breakTie,
        List<String> breakTieTie) {
}
