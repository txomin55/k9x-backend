package com.k9x.domain.competitions.aggregates;

import java.util.List;

/**
 * Provenance of a competition k9x did not collect itself: which extraction loaded it, where the evidence was
 * taken from and when it was collected.
 *
 * <p>{@code type} is stored as a closed vocabulary plus comma-separated parameters, e.g.
 * {@code FEDERATION_PAGE,cpc} or {@code PRIVATE_CONVERSATIONS,ORGANIZER}: the ETL refuses parameters containing
 * a comma, so the first token is always the type and the rest are its parameters. It is kept raw here because
 * turning it into a sentence is a presentation concern.
 *
 * <p>{@code restricted} says the source forbids republishing these results. It changes nothing here: the scores
 * are stored, snapshotted and ranked exactly like any others, and it is the REST boundary that withholds them —
 * a restricted competition answers with null scores and without saying where the evidence came from.
 *
 * <p>{@code loadedAt} is when the ETL loaded the extraction into k9x, epoch millis. It is not
 * {@code extractionTimestamp}: the evidence can be collected months before it is loaded.
 */
public record CompetitionExtraction(String extractionId, String url, Long extractionTimestamp, String type,
                                    boolean restricted, Long loadedAt) {

    /**
     * A competition whose {@code source} says EXTRACTION but has no metadata row yet: the reader still has to be
     * told the results were not collected by k9x, even when nobody wrote down where they came from.
     */
    public static final CompetitionExtraction UNKNOWN = new CompetitionExtraction(null, null, null, null, false, null);

    /** First token of {@code type}, or {@code null} when there is no type at all. */
    public String typeToken() {
        if (type == null || type.isBlank()) {
            return null;
        }
        return type.split(",")[0].trim();
    }

    /** Everything after the first token of {@code type}, in order. Empty when the type carries no parameters. */
    public List<String> typeParams() {
        if (type == null || type.isBlank()) {
            return List.of();
        }
        String[] tokens = type.split(",");
        return java.util.Arrays.stream(tokens).skip(1).map(String::trim).filter(t -> !t.isEmpty()).toList();
    }
}
