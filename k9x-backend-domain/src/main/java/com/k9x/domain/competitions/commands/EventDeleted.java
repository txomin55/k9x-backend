package com.k9x.domain.competitions.commands;

public record EventDeleted(String id, long deletedAt) implements CompetitionChange {
}
