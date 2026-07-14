package com.k9x.application.events.snapshot.port;

import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;

import java.util.Optional;

public interface GetEventSnapshotPersistencePort {
    Optional<FetchClassificationDTO> getSnapshot(String eventId);
}
