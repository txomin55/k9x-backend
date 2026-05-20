package com.k9x.application.events.obdx.port;

public interface DeleteObdxEventPersistencePort {

    void deleteEvent(String id, long deletedAt);
}
