package com.k9x.domain.competitions.aggregates;

import java.util.Locale;

/**
 * Where a competition's data comes from. Everything created through the app is {@link #API}; {@link #EXTRACTION}
 * marks competitions loaded by an external ETL, whose results k9x did not collect and therefore surfaces with a
 * warning to the reader.
 */
public enum CompetitionSource {
    API,
    EXTRACTION;

    /**
     * Never fails on unknown or missing stored values: the column is informational, and a competition with a
     * source k9x does not know about is still a perfectly readable competition, so it falls back to {@link #API}.
     */
    public static CompetitionSource fromStored(String stored) {
        if (stored == null) {
            return API;
        }
        try {
            return valueOf(stored.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return API;
        }
    }
}
