package com.k9x.application.events.snapshot.port;

import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;

import java.util.Optional;

public interface GetObdxEventSnapshotPersistencePort {
    Optional<FetchObdxClassificationDTO> getSnapshot(String eventId);
}
