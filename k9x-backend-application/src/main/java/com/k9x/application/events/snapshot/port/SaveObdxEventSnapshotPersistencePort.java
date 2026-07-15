package com.k9x.application.events.snapshot.port;

import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;

public interface SaveObdxEventSnapshotPersistencePort {
    void save(String eventId, long timestamp, FetchClassificationDTO snapshot);
}
