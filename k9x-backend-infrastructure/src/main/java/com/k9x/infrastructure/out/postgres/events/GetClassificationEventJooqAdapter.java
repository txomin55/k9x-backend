package com.k9x.infrastructure.out.postgres.events;

import com.k9x.application.events.port.GetClassificationEventPersistencePort;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.infrastructure.out.postgres.competitions.CompetitionHydrator;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Optional;

/**
 * Loads the one event a live classification is computed from, with its competitors, exercises, judges and
 * scores, through {@link CompetitionHydrator#hydrateEvents} — never its stage's or competition's other events.
 */
public class GetClassificationEventJooqAdapter implements GetClassificationEventPersistencePort {

    private final CompetitionHydrator hydrator;

    public GetClassificationEventJooqAdapter(DSLContext dsl) {
        this.hydrator = new CompetitionHydrator(dsl);
    }

    @Override
    public Optional<EventSnapshot> getEvent(String eventId) {
        return hydrator.hydrateEvents(List.of(eventId)).stream().findFirst();
    }
}
