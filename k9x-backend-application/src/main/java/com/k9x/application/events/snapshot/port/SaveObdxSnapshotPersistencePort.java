package com.k9x.application.events.snapshot.port;

import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.snapshot.port.payload.ObdxCompetitorPosition;

import java.util.List;

/**
 * Atomically persists an OBDX event's classification snapshot: the per-competitor position, total score and
 * rank score on {@code obdx.snap_event_competitors_results}, the dog's rank history row on
 * {@code k9x.snap_dog_rank}, together with the snapshot marker row holding the OBDX classification payload
 * (only the heavy computed part; event metadata is recovered on read). All writes for an event happen in a
 * single transaction, so a failure leaves the event without a snapshot and it is retried on the next run.
 *
 * <p>{@code snapshotAt} is the persistence instant (audit only); {@code applyingAt} is the instant the results
 * apply to — the event's stage end — and is what every consumer (index, degradation, ordering) uses.
 */
public interface SaveObdxSnapshotPersistencePort {

    void save(String eventId, long snapshotAt, long applyingAt, FetchObdxClassificationDTO obdx,
              List<ObdxCompetitorPosition> competitors);
}
