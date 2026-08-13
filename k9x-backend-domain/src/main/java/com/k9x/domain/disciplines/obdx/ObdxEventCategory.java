package com.k9x.domain.disciplines.obdx;

/**
 * The competitive tier of an OBDX event, as declared by the organizer: a club trial, an open trial or one of
 * the World Championship rounds (qualifier, semi-final, final).
 */
public enum ObdxEventCategory {
    CLUB,
    OPEN,
    WC_Q,
    WC_SEMI,
    WC_FINAL;

    /**
     * Resolves the category from its persisted / requested name, tolerating {@code null} and blank values: an
     * event may legitimately carry no category (the field is optional and only OBDX events have one).
     */
    public static ObdxEventCategory fromName(String name) {
        return name == null || name.isBlank() ? null : valueOf(name);
    }
}
