package com.k9x.application.events.obdx.port;

public interface GetObdxEventCollectorPersistencePort {

    String getCollectorId(String eventId, String judgeId);
}
