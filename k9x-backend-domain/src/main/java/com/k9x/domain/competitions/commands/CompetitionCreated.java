package com.k9x.domain.competitions.commands;

public record CompetitionCreated(String id, String name, String creator, long createdAt)
        implements CompetitionChange {
}
