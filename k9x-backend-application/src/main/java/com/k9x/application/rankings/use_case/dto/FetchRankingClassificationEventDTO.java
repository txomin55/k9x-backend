package com.k9x.application.rankings.use_case.dto;

/**
 * A column of the results matrix. The trial id travels along so the event classification can be linked to.
 *
 * <p>{@code restricted} says this column's competition may not be republished: it is aggregated like any other —
 * the group totals are the real ones — and it is the REST boundary that empties its cells.
 */
public record FetchRankingClassificationEventDTO(String id, String name, String stageId, boolean restricted) {
}
