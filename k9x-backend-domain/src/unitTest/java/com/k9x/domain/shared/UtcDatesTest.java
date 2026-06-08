package com.k9x.domain.shared;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtcDatesTest {

    private static long millis(String isoInstant) {
        return Instant.parse(isoInstant).toEpochMilli();
    }

    @Test
    void same_utc_day_is_true_for_different_times_of_the_same_day() {
        assertTrue(UtcDates.isSameUtcDay(millis("2024-06-15T00:00:01Z"), millis("2024-06-15T23:59:59Z")));
    }

    @Test
    void same_utc_day_is_false_across_the_utc_midnight_boundary() {
        assertFalse(UtcDates.isSameUtcDay(millis("2024-06-15T23:59:59Z"), millis("2024-06-16T00:00:01Z")));
    }

    @Test
    void is_after_utc_day_is_true_only_when_strictly_a_later_day() {
        assertTrue(UtcDates.isAfterUtcDay(millis("2024-06-16T00:00:00Z"), millis("2024-06-15T23:00:00Z")));
        assertFalse(UtcDates.isAfterUtcDay(millis("2024-06-15T23:00:00Z"), millis("2024-06-15T01:00:00Z")));
        assertFalse(UtcDates.isAfterUtcDay(millis("2024-06-14T23:00:00Z"), millis("2024-06-15T01:00:00Z")));
    }

    @Test
    void is_before_utc_day_is_true_only_when_strictly_an_earlier_day() {
        assertTrue(UtcDates.isBeforeUtcDay(millis("2024-06-14T23:00:00Z"), millis("2024-06-15T01:00:00Z")));
        assertFalse(UtcDates.isBeforeUtcDay(millis("2024-06-15T01:00:00Z"), millis("2024-06-15T23:00:00Z")));
    }
}
