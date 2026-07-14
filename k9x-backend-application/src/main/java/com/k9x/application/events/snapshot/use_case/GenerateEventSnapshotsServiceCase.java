package com.k9x.application.events.snapshot.use_case;

import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.snapshot.port.GetPendingSnapshotEventsPersistencePort;
import com.k9x.application.events.snapshot.port.SaveEventSnapshotPersistencePort;
import com.k9x.application.events.use_case.GetEventClassificationServiceCase;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.shared.UtcDates;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;

/**
 * Persists a classification snapshot for every event whose stage has already finished and that does not yet
 * have one. Meant to be triggered once a day by a scheduler. Each event is snapshotted independently: a failure
 * computing or storing one event is logged and skipped so it does not abort the rest of the batch.
 */
public class GenerateEventSnapshotsServiceCase {

    private static final Logger log = System.getLogger(GenerateEventSnapshotsServiceCase.class.getName());

    private final GetPendingSnapshotEventsPersistencePort getPendingSnapshotEventsPersistencePort;
    private final GetEventClassificationServiceCase getEventClassificationServiceCase;
    private final SaveEventSnapshotPersistencePort saveEventSnapshotPersistencePort;

    public GenerateEventSnapshotsServiceCase(
            GetPendingSnapshotEventsPersistencePort getPendingSnapshotEventsPersistencePort,
            GetEventClassificationServiceCase getEventClassificationServiceCase,
            SaveEventSnapshotPersistencePort saveEventSnapshotPersistencePort) {
        this.getPendingSnapshotEventsPersistencePort = getPendingSnapshotEventsPersistencePort;
        this.getEventClassificationServiceCase = getEventClassificationServiceCase;
        this.saveEventSnapshotPersistencePort = saveEventSnapshotPersistencePort;
    }

    public void generateSnapshots() {
        long now = DateUtils.nowUtcMillis();
        long startOfToday = UtcDates.startOfUtcDay(now);

        List<String> eventIds = getPendingSnapshotEventsPersistencePort.getFinishedEventsWithoutSnapshot(startOfToday);
        log.log(Level.INFO, "Generating classification snapshots for {0} finished event(s)", eventIds.size());

        for (String eventId : eventIds) {
            try {
                FetchClassificationDTO classification = getEventClassificationServiceCase.getClassification(eventId);
                saveEventSnapshotPersistencePort.save(eventId, now, classification);
            } catch (RuntimeException e) {
                log.log(Level.ERROR, "Failed to generate classification snapshot for event " + eventId, e);
            }
        }
    }
}
