package com.k9x.application.events.snapshot.use_case;

import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.snapshot.port.GetObdxEventSnapshotPersistencePort;
import com.k9x.domain.disciplines.valueobjects.Discipline;

import java.util.Optional;

/**
 * Returns the persisted classification snapshot for an event, if one exists. Dispatches to the right
 * discipline-specific store (today only OBDX) so callers ask for a snapshot by event id and discipline
 * without knowing where it lives. The discipline must be resolved first (from the event context), so this
 * cannot short-circuit before the event context is loaded.
 */
public class GetEventSnapshotServiceCase {

    private final GetObdxEventSnapshotPersistencePort getObdxEventSnapshotPersistencePort;

    public GetEventSnapshotServiceCase(GetObdxEventSnapshotPersistencePort getObdxEventSnapshotPersistencePort) {
        this.getObdxEventSnapshotPersistencePort = getObdxEventSnapshotPersistencePort;
    }

    public Optional<FetchObdxClassificationDTO> getSnapshot(String eventId, String disciplineId) {
        return switch (Discipline.fromStored(disciplineId)) {
            case OBDX -> getObdxEventSnapshotPersistencePort.getSnapshot(eventId);
        };
    }
}
