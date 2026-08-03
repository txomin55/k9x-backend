package com.k9x.application.dogs.rank.use_case.dto;

/**
 * The most recent {@code k9x.snap_dog_index_history} record of a dog in a discipline (by
 * {@code applying_timestamp}, the effective instant of the record — not the persistence timestamp).
 */
public record FetchLatestDogRankHistoryDTO(String dogId, String discipline, int rank, long applyingTimestamp) {
}
