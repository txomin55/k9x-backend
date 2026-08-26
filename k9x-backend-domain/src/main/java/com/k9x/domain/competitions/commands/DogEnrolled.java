package com.k9x.domain.competitions.commands;

import com.k9x.domain.events.valueobjects.CompetitorDogSnapshot;

public record DogEnrolled(String eventId, String dogIdentification, boolean bih, String primer, short startNumber,
                          CompetitorDogSnapshot dogSnapshot, long lastUpdate) implements CompetitionChange {

    public DogEnrolled {
        dogSnapshot = dogSnapshot == null ? CompetitorDogSnapshot.EMPTY : dogSnapshot;
    }
}
