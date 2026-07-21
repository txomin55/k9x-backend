package com.k9x.application.events.snapshot.port;

import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.snapshot.port.payload.ObdxCompetitorPosition;

import java.util.List;

/**
 * Atomically persists an OBDX event's classification snapshot: the per-competitor position and rank score on
 * {@code obdx.event_competitors}, the event-level list of granted awards on {@code k9x.events.granted_awards},
 * together with the snapshot marker row holding the OBDX classification payload (only the heavy computed part;
 * event metadata is recovered on read). All writes for an event happen in a single transaction, so a failure
 * leaves the event without a snapshot and it is retried on the next run.
 */
public interface SaveObdxSnapshotPersistencePort {

    void save(String eventId, long snapshotAt, FetchObdxClassificationDTO obdx,
              List<ObdxCompetitorPosition> competitors, List<String> grantedAwards);
}
