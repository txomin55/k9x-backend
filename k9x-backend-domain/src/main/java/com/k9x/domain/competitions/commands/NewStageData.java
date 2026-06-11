package com.k9x.domain.competitions.commands;

/**
 * Inbound data for creating a stage on a {@link com.k9x.domain.competitions.aggregates.CompetitionAggregate}.
 * Application service cases map their command into this domain record.
 */
public record NewStageData(String id, String name, Long dateFrom, Long dateTo) {
}
