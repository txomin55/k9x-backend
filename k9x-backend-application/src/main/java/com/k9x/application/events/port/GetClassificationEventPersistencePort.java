package com.k9x.application.events.port;

import com.k9x.domain.events.aggregates.EventSnapshot;

import java.util.Optional;

public interface GetClassificationEventPersistencePort {

    /**
     * Just this event, with its competitors, exercises, judges and scores: the input of a live classification.
     * Its stage and competition are not loaded.
     */
    Optional<EventSnapshot> getEvent(String eventId);
}
