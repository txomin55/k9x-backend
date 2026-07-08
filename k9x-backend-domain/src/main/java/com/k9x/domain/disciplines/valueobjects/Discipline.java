package com.k9x.domain.disciplines.valueobjects;

import com.k9x.domain.disciplines.exceptions.DisciplineConfigurationMalformedException;
import com.k9x.domain.disciplines.exceptions.DisciplineNotFoundException;

import java.util.Locale;
import java.util.Optional;

public enum Discipline {
    OBDX;

    /**
     * Resolves a discipline coming from a client request (path/query param). Matching is strict: only an
     * exact match is accepted (e.g. {@code "obdx"} is rejected). An unknown or {@code null} value means
     * the requested discipline does not exist → {@link DisciplineNotFoundException} (404).
     */
    public static Discipline fromRequest(String value) {
        return parse(value).orElseThrow(DisciplineNotFoundException::new);
    }

    /**
     * Resolves a discipline coming from persisted/configuration data. Matching is case-insensitive to
     * tolerate historically stored values. An unknown or {@code null} value means the stored data is
     * corrupt → {@link DisciplineConfigurationMalformedException} (409).
     */
    public static Discipline fromStored(String value) {
        String normalized = value == null ? null : value.toUpperCase(Locale.ROOT);
        return parse(normalized).orElseThrow(DisciplineConfigurationMalformedException::new);
    }

    private static Optional<Discipline> parse(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(value));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
