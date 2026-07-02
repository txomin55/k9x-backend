package com.k9x.domain.shared;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
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

    /** The given instant's UTC calendar day at 00:00:00.000. */
    public static long startOfUtcDay(long epochMillis) {
        return utcDay(epochMillis).atStartOfDay(UTC).toInstant().toEpochMilli();
    }

    /** The given instant's UTC calendar day at 23:59:59.999. */
    public static long endOfUtcDay(long epochMillis) {
        return utcDay(epochMillis).atTime(LocalTime.MAX).atZone(UTC).toInstant().toEpochMilli();
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
