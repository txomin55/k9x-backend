package com.k9x.application.events.obdx.port;

import com.k9x.application.events.obdx.port.payload.UpdateObdxScorePersistencePayload;

public interface UpdateObdxScorePersistencePort {

    void updateScore(String eventId, UpdateObdxScorePersistencePayload payload);
}
