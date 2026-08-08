package com.k9x.application.events.snapshot.port.payload;

import java.math.BigDecimal;

/**
 * A competitor's snapshot outcome to persist on {@code obdx.snap_event_competitors_results}: their final {@code position},
 * the raw weighted {@code totalScore} they achieved in the event (may be {@code null} when they never scored)
 * and their own {@code rankScore} (may be {@code null} when the competitor has no score or the event carries
 * no rank score).
 */
public record ObdxCompetitorPosition(String dogIdentification, short position, BigDecimal totalScore, BigDecimal rankScore) {
}
