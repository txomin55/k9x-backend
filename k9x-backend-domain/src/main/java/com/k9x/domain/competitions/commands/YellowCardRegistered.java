package com.k9x.domain.competitions.commands;

public record YellowCardRegistered(String eventId, String judgeId, String exerciseId, String dogId,
                                   long lastUpdate) implements CompetitionChange {
}
