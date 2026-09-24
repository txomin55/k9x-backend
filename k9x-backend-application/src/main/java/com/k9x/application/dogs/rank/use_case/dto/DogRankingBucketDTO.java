package com.k9x.application.dogs.rank.use_case.dto;

/** A band of the ranking chart: dogs whose index is in {@code [from, to)}, the last band closed at 1000. */
public record DogRankingBucketDTO(int from, int to, int dogs) {
}
