package com.k9x.application.dogs.rank.use_case.dto;

import java.math.BigDecimal;

/**
 * One snapshotted event result of a dog: the discipline it was earned in, its rank score in that event and the
 * instant it applies to (the event's stage end — not when it was persisted).
 */
public record FetchDogRankEventResultDTO(String dogId, String discipline, String eventId, BigDecimal rank,
                                         long applyingTimestamp) {
}
