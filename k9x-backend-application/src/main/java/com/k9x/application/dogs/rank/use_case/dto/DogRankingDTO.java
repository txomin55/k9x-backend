package com.k9x.application.dogs.rank.use_case.dto;

import java.util.List;

/**
 * The world ranking chart. {@code total} is the number of charted dogs, {@code asOf} when the snapshot was
 * computed ({@code null} while it is empty) and {@code highlight} the requested dog, {@code null} when none was
 * requested or it is not on the chart.
 */
public record DogRankingDTO(int total, Long asOf, List<DogRankingBucketDTO> buckets,
                            DogRankingHighlightDTO highlight) {
}
