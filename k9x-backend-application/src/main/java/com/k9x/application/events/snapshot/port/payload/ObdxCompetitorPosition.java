package com.k9x.application.events.snapshot.port.payload;

import java.math.BigDecimal;

/**
 * A competitor's snapshot outcome to persist on {@code obdx.event_competitors}: their final {@code position}
 * and their own {@code rankScore} (may be {@code null} when the competitor has no score or the event carries
 * no rank score).
 */
public record ObdxCompetitorPosition(String dogId, short position, BigDecimal rankScore) {
}
