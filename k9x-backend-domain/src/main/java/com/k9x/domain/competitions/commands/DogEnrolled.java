package com.k9x.domain.competitions.commands;

public record DogEnrolled(String eventId, String dogId, boolean bih, short position, long lastUpdate) implements CompetitionChange {
}
