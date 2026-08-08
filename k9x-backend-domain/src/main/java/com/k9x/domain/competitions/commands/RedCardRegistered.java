package com.k9x.domain.competitions.commands;

public record RedCardRegistered(String eventId, String judgeId, String exerciseId, String dogIdentification,
                                long lastUpdate) implements CompetitionChange {
}
