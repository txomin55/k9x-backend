package com.k9x.application.events.snapshot.port;

import java.util.List;

public interface GetPendingSnapshotEventsPersistencePort {
    /**
     * Event ids whose stage has already finished (stage {@code date_to} before {@code startOfTodayUtcMillis})
     * and that do not yet have a row in {@code obdx.event_snapshot}. Soft-deleted events and stages are excluded.
     */
    List<String> getFinishedEventsWithoutSnapshot(long startOfTodayUtcMillis);
}
