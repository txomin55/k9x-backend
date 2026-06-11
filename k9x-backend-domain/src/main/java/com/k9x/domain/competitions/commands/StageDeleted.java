package com.k9x.domain.competitions.commands;

public record StageDeleted(String id, long deletedAt) implements CompetitionChange {
}
