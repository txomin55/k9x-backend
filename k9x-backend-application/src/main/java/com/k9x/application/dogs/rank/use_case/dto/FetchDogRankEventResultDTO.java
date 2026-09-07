package com.k9x.application.dogs.rank.use_case.dto;

import java.math.BigDecimal;

/**
 * One snapshotted event result of a dog: its rank score in that event and the instant it applies to (the
 * event's stage end — not when it was persisted). The discipline the result was earned in is deliberately not
 * carried: the index is a single per-dog figure computed over every result, whatever discipline produced it.
 */
public record FetchDogRankEventResultDTO(String dogIdentification, String eventId, BigDecimal rank,
                                         long applyingTimestamp) {
}
