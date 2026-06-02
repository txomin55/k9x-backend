package com.k9x.application.events.obdx.use_cases.port;

import com.k9x.domain.aggregates.events.Event;

public interface GetEventPersistencePort {

    Event getEvent(String id);
}
