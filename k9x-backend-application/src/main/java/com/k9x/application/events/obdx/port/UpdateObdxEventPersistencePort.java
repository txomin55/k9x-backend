package com.k9x.application.events.obdx.port;

import com.k9x.application.events.obdx.port.payload.UpdateObdxEventPersistencePayload;

public interface UpdateObdxEventPersistencePort {

    void updateEvent(String id, UpdateObdxEventPersistencePayload payload);
}
