package com.k9x.application.dogs.use_case.dto;

import java.math.BigDecimal;

/**
 * One event a dog was entered in. {@code position}, {@code totalScore} and {@code rankScore} come from
 * {@code obdx.snap_event_competitors_results} and are {@code null} until the snapshot cron has run for the event.
 * {@code restricted} says the competition's latest extraction forbids republishing its results; the scores are
 * carried anyway and withheld at the REST boundary, like every other restricted read.
 */
public record DogParticipationDTO(String eventId, String eventName, String stageId, long stageDateFrom,
                                  String country, Short position, BigDecimal totalScore, BigDecimal rankScore,
                                  boolean restricted) {
}
