package com.k9x.application.rankings.use_case.dto;

/**
 * A selectable ranking criterion: the enum value as {@code id} plus its translated label as {@code name}.
 */
public record RankingCriterionDTO(String id, String name) {
}
