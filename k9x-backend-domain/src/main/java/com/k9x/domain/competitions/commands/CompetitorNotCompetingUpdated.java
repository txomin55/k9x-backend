package com.k9x.domain.competitions.commands;

public record CompetitorNotCompetingUpdated(String eventId, String dogIdentification, boolean notCompeting,
                                            long lastUpdate) implements CompetitionChange {
}
