package com.k9x.application.dogs.rank.use_case.dto;

/** A dog's row of the ranking snapshot, with its name for display. */
public record FetchDogRankingEntryDTO(String dogIdentification, String name, int index, String country) {
}
