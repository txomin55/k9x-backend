package com.k9x.application.rankings.use_case.dto;

/** A column of the results matrix. The trial id travels along so the event classification can be linked to. */
public record FetchRankingClassificationEventDTO(String id, String name, String stageId) {
}
