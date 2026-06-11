package com.k9x.domain.competitions.commands;

public record EventCreated(String id, String name, String stageId, String discipline, String creator,
                           long createdAt) implements CompetitionChange {
}
