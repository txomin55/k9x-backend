package com.k9x.application.events.obdx.port;

public interface CreateObdxEventPersistencePort {

    void createEvent(String id, String name, String stageId, String creator, long createdAt);
}
