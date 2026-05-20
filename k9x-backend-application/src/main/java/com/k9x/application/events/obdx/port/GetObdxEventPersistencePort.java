package com.k9x.application.events.obdx.port;

import com.k9x.domain.aggregates.events.obdx.ObdxEvent;

public interface GetObdxEventPersistencePort {

    ObdxEvent getEvent(String id);
}
