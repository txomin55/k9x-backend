package com.k9x.application.events.obdx.use_case.dto;

import com.k9x.domain.disciplines.valueobjects.ClassificationCacheEvictStrategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ObdxClassificationConfigDTO(
        ClassificationCacheEvictStrategy cacheEvictStrategy,
        BigDecimal maxAllowedScore,
        Map<String, BigDecimal> coefByExerciseId,
        List<String> breakTie,
        List<String> breakTieTie,
        List<QualificationThreshold> qualifications) {

    /**
     * A text qualification tier keyed by an i18n-resolvable id (e.g. {@code OBDX_QUALIFICATION_EXC}) and
     * the minimum absolute total score required to reach it. The applicable qualification for a competitor
     * is the highest tier whose {@code minScore} its total score reaches; below the lowest tier the
     * competitor is {@code NC} (No clasificado).
     */
    public record QualificationThreshold(String id, BigDecimal minScore) {
    }
}
