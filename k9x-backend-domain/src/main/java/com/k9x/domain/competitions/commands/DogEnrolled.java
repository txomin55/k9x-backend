package com.k9x.domain.competitions.commands;

public record DogEnrolled(String eventId, String dogId, long lastUpdate) implements CompetitionChange {
}
