package com.k9x.domain.competitions.commands;

public record DogEnrolled(String eventId, String dogIdentification, boolean bih, String primer, short startNumber, long lastUpdate) implements CompetitionChange {
}
