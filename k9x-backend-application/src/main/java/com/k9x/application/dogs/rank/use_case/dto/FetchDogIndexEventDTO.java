package com.k9x.application.dogs.rank.use_case.dto;

import java.math.BigDecimal;

/**
 * One result that feeds a dog's index: {@code rankScore} and {@code appliesAt} are the {@code k9x.snap_dog_rank}
 * row the index is computed from, the rest describes where it was earned. {@code position} and
 * {@code totalScore} come from {@code obdx.snap_event_competitors_results}. {@code restricted} says the
 * competition's latest extraction forbids republishing its results; the scores are carried anyway and withheld at
 * the REST boundary, like every other restricted read.
 */
public record FetchDogIndexEventDTO(String eventId, String eventName, String stageId, String discipline,
                                    String country, long appliesAt, BigDecimal rankScore, Short position,
                                    BigDecimal totalScore, boolean restricted) {
}
