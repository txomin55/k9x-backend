package com.k9x.application.events.obdx.port;

import com.k9x.application.events.obdx.port.payload.EnrollObdxEventPersistencePayload;

public interface EnrollObdxEventPersistencePort {

    void enrollEvent(String eventId, EnrollObdxEventPersistencePayload payload);
}
