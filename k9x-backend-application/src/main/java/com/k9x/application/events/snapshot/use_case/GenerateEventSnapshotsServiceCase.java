package com.k9x.application.events.snapshot.use_case;

import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.snapshot.port.GetPendingSnapshotEventsPersistencePort;
import com.k9x.application.events.snapshot.port.SaveObdxSnapshotPersistencePort;
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
    private final SaveObdxSnapshotPersistencePort saveObdxSnapshotPersistencePort;

    public GenerateEventSnapshotsServiceCase(
            GetPendingSnapshotEventsPersistencePort getPendingSnapshotEventsPersistencePort,
            GetEventClassificationServiceCase getEventClassificationServiceCase,
            SaveObdxSnapshotPersistencePort saveObdxSnapshotPersistencePort) {
        this.getPendingSnapshotEventsPersistencePort = getPendingSnapshotEventsPersistencePort;
        this.getEventClassificationServiceCase = getEventClassificationServiceCase;
        this.saveObdxSnapshotPersistencePort = saveObdxSnapshotPersistencePort;
    }

    /**
     * @return how many snapshots were written, so a manual run can report it.
     */
    public int generateSnapshots() {
        long now = DateUtils.nowUtcMillis();
        long startOfToday = UtcDates.startOfUtcDay(now);

        List<PendingSnapshotEventDTO> pendingEvents =
                getPendingSnapshotEventsPersistencePort.getFinishedEventsWithoutSnapshot(startOfToday);
        log.log(Level.INFO, "Generating classification snapshots for {0} finished event(s) ({1})",
                pendingEvents.size(), heap());

        int generated = 0;
        int failed = 0;
        for (int i = 0; i < pendingEvents.size(); i++) {
            PendingSnapshotEventDTO pending = pendingEvents.get(i);
            try {
                snapshot(pending, now);
                generated++;
            } catch (RuntimeException e) {
                failed++;
                log.log(Level.ERROR, "Failed to generate classification snapshot for event " + pending.eventId(), e);
            } catch (Error e) {
                // An OutOfMemoryError is not a RuntimeException, so it ends the batch; say where it happened
                // before it propagates. On 2026-09-24 it died after 587 events and the log never said which one.
                log.log(Level.ERROR, "Classification snapshots aborted at event " + pending.eventId() + " ("
                        + (i + 1) + "/" + pendingEvents.size() + ", " + heap() + ")", e);
                throw e;
            }
        }
        log.log(Level.INFO, "Generated {0} classification snapshot(s), {1} failed ({2})", generated, failed, heap());
        return generated;
    }

    private static String heap() {
        Runtime runtime = Runtime.getRuntime();
        long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        return "heap " + usedMb + "/" + runtime.maxMemory() / (1024 * 1024) + " MB";
    }

    private void snapshot(PendingSnapshotEventDTO pending, long now) {
        Discipline discipline = Discipline.fromStored(pending.discipline());
        switch (discipline) {
            case OBDX -> {
                FetchClassificationDTO classification =
                        getEventClassificationServiceCase.getClassification(pending.eventId());
                // Persist the already-computed (tie-aware) ranking into snap_event_competitors_results
                // (position + total score + the competitor's own rank_score) together with the snapshot
                // marker, atomically: the port writes both inside one transaction, so a failure leaves the
                // event without a snapshot and it is retried on the next run (the writes are idempotent, so a
                // retry re-stamps the same values).
                List<ObdxCompetitorPosition> competitors = classification.obdx() == null ? List.of()
                        : classification.obdx().competitors().stream()
                                .map(c -> new ObdxCompetitorPosition(c.dogIdentification(), (short) c.position(),
                                        c.totalScore(), c.rankScore()))
                                .toList();
                saveObdxSnapshotPersistencePort.save(pending.eventId(), now, pending.stageEndAt(),
                        classification.obdx(), competitors);
            }
        }
    }
}
