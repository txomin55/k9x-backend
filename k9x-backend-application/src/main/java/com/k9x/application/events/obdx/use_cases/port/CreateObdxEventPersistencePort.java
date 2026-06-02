package com.k9x.application.events.obdx.use_cases.port;

public interface CreateObdxEventPersistencePort {

    void createEvent(String id, String name, String stageId, String discipline, String creator, long createdAt);
}
