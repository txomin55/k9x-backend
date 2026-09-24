package com.k9x.application.dogs.rank.use_case.dto;

import java.util.Map;

/**
 * Dog count per index of the ranking snapshot. {@code computedAt} is when the snapshot was written, {@code null}
 * when it holds no dog.
 */
public record FetchDogRankingDistributionDTO(Long computedAt, Map<Integer, Integer> dogsByIndex) {
}
