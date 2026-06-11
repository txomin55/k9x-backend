package com.k9x.domain.competitions.commands;

public record StageCreated(String id, String name, String competitionId, Long dateFrom, Long dateTo,
                           String creator, long createdAt) implements CompetitionChange {
}
