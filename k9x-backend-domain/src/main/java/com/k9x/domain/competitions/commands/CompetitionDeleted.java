package com.k9x.domain.competitions.commands;

public record CompetitionDeleted(String id, long deletedAt) implements CompetitionChange {
}
