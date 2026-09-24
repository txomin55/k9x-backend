package com.k9x.application.dogs.rank.use_case.dto;

/** A dog placed on the ranking chart. */
public record DogRankingHighlightDTO(String dogIdentification, String name, int index, int position,
                                     int topPercent) {
}
