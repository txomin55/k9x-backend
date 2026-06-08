package com.k9x.domain.shared;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Day-boundary helpers in UTC. All timestamps across the system are epoch milliseconds in UTC,
 * so lifecycle status that reasons about "same day" / "next day" truncates to the UTC calendar day.
 */
public final class UtcDates {

    private static final ZoneId UTC = ZoneId.of("UTC");

    private UtcDates() {}

    public static LocalDate utcDay(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(UTC).toLocalDate();
    }

    public static boolean isSameUtcDay(long epochMillisA, long epochMillisB) {
        return utcDay(epochMillisA).isEqual(utcDay(epochMillisB));
    }

    /** True when the UTC day of {@code instant} is strictly after the UTC day of {@code reference}. */
    public static boolean isAfterUtcDay(long instant, long reference) {
        return utcDay(instant).isAfter(utcDay(reference));
    }

    /** True when the UTC day of {@code instant} is strictly before the UTC day of {@code reference}. */
    public static boolean isBeforeUtcDay(long instant, long reference) {
        return utcDay(instant).isBefore(utcDay(reference));
    }
}
