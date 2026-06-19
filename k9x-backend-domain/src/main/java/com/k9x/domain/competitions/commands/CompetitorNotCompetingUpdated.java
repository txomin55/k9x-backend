package com.k9x.domain.competitions.commands;

public record CompetitorNotCompetingUpdated(String eventId, String dogId, boolean notCompeting,
                                            long lastUpdate) implements CompetitionChange {
}
