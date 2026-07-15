package com.k9x.application.events.snapshot.port;

import com.k9x.application.events.snapshot.use_case.dto.PendingSnapshotEventDTO;

import java.util.List;

public interface GetPendingSnapshotEventsPersistencePort {
    /**
     * Events whose stage has already finished (stage {@code date_to} before {@code startOfTodayUtcMillis})
     * and that do not yet have a row in {@code obdx.event_snapshot}, each with its stored discipline.
     * Soft-deleted events and stages are excluded.
     */
    List<PendingSnapshotEventDTO> getFinishedEventsWithoutSnapshot(long startOfTodayUtcMillis);
}
