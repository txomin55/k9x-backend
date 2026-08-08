package com.k9x.application.utils.text;

/**
 * Helpers for guarding free-text identifier strings (ids, identifications, origins) in the service-case layer.
 * A blank identifier ({@code null} or empty/whitespace-only) is treated as "not provided", so callers never
 * run existence/collision queries against an empty value (which would spuriously match rows with empty columns).
 */
public final class Identifiers {

    private Identifiers() {}

    /** True when the identifier carries an actual value (not {@code null} and not blank). */
    public static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    /** True when the identifier is {@code null} or blank. */
    public static boolean isBlank(String value) {
        return !isPresent(value);
    }
}
