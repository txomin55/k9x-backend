package com.k9x.domain.competitions.commands;

public record StageRenamed(String id, String name, Long dateFrom, Long dateTo, long lastUpdate)
        implements CompetitionChange {
}
