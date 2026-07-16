package com.k9x.application.events.snapshot.use_case;

import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.snapshot.port.GetPendingSnapshotEventsPersistencePort;
import com.k9x.application.events.snapshot.port.SaveObdxEventSnapshotPersistencePort;
import com.k9x.application.events.snapshot.port.UpdateObdxCompetitorPositionsPersistencePort;
import com.k9x.application.events.snapshot.port.payload.ObdxCompetitorPosition;
import com.k9x.application.events.snapshot.use_case.dto.PendingSnapshotEventDTO;
import com.k9x.application.events.use_case.GetEventClassificationServiceCase;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.disciplines.valueobjects.Discipline;
import com.k9x.domain.shared.UtcDates;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;

/**
 * Persists a classification snapshot for every event whose stage has already finished and that does not yet
 * have one. Meant to be triggered once a day by a scheduler. Generation dispatches per discipline: today only
 * OBDX produces a classification, so only OBDX events are snapshotted; adding a discipline means adding its
 * branch to {@link #snapshot}. Each event is processed independently: a failure computing or storing one event
 * is logged and skipped so it does not abort the rest of the batch.
 */
public class GenerateEventSnapshotsServiceCase {

    private static final Logger log = System.getLogger(GenerateEventSnapshotsServiceCase.class.getName());

    private final GetPendingSnapshotEventsPersistencePort getPendingSnapshotEventsPersistencePort;
    private final GetEventClassificationServiceCase getEventClassificationServiceCase;
    private final SaveObdxEventSnapshotPersistencePort saveObdxEventSnapshotPersistencePort;
    private final UpdateObdxCompetitorPositionsPersistencePort updateObdxCompetitorPositionsPersistencePort;

    public GenerateEventSnapshotsServiceCase(
            GetPendingSnapshotEventsPersistencePort getPendingSnapshotEventsPersistencePort,
            GetEventClassificationServiceCase getEventClassificationServiceCase,
            SaveObdxEventSnapshotPersistencePort saveObdxEventSnapshotPersistencePort,
            UpdateObdxCompetitorPositionsPersistencePort updateObdxCompetitorPositionsPersistencePort) {
        this.getPendingSnapshotEventsPersistencePort = getPendingSnapshotEventsPersistencePort;
        this.getEventClassificationServiceCase = getEventClassificationServiceCase;
        this.saveObdxEventSnapshotPersistencePort = saveObdxEventSnapshotPersistencePort;
        this.updateObdxCompetitorPositionsPersistencePort = updateObdxCompetitorPositionsPersistencePort;
    }

    public void generateSnapshots() {
        long now = DateUtils.nowUtcMillis();
        long startOfToday = UtcDates.startOfUtcDay(now);

        List<PendingSnapshotEventDTO> pendingEvents =
                getPendingSnapshotEventsPersistencePort.getFinishedEventsWithoutSnapshot(startOfToday);
        log.log(Level.INFO, "Generating classification snapshots for {0} finished event(s)", pendingEvents.size());

        for (PendingSnapshotEventDTO pending : pendingEvents) {
            try {
                snapshot(pending, now);
            } catch (RuntimeException e) {
                log.log(Level.ERROR, "Failed to generate classification snapshot for event " + pending.eventId(), e);
            }
        }
    }

    private void snapshot(PendingSnapshotEventDTO pending, long now) {
        Discipline discipline = Discipline.fromStored(pending.discipline());
        switch (discipline) {
            case OBDX -> {
                FetchClassificationDTO classification =
                        getEventClassificationServiceCase.getClassification(pending.eventId());
                // Persist the already-computed (tie-aware) ranking into event_competitors.position, then store
                // the snapshot. Saving the snapshot last makes it the "done" marker: if the position write
                // fails, the event is left without a snapshot and retried on the next run (the UPDATE is
                // idempotent, so a retry simply re-stamps the same values).
                List<ObdxCompetitorPosition> positions = classification.obdx() == null ? List.of()
                        : classification.obdx().competitors().stream()
                                .map(c -> new ObdxCompetitorPosition(c.dogId(), (short) c.position()))
                                .toList();
                updateObdxCompetitorPositionsPersistencePort.updatePositions(pending.eventId(), positions);
                saveObdxEventSnapshotPersistencePort.save(pending.eventId(), now, classification);
            }
        }
    }
}
