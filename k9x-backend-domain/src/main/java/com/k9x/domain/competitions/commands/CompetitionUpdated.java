package com.k9x.domain.competitions.commands;

public record CompetitionUpdated(String id, String name, String description, String country, String address,
                                 Double coordAlt, Double coordLong, long lastUpdate) implements CompetitionChange {
}
