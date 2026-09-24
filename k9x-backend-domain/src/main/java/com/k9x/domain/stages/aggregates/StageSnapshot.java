package com.k9x.domain.stages.aggregates;

import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.stages.status.StageLifecycle;
import com.k9x.domain.stages.status.StageStatus;

import java.util.List;

public record StageSnapshot(
        String id,
        String name,
        String competitionId,
        String creator,
        long dateFrom,
        long dateTo,
        long lastUpdate,
        long createdAt,
        Long deletedAt,
        List<EventSnapshot> events
) {

    /** Lifecycle status, resolved by {@link StageLifecycle#status} over this stage's events. */
    public StageStatus status(long now) {
        return StageLifecycle.status(deletedAt, now, dateFrom, dateTo,
                () -> events == null ? List.of() : events.stream().map(event -> event.status(now, dateTo)).toList(),
                this::hasAnyScore);
    }

    /** Whether the given event of this stage accepts enrollments, see {@link StageLifecycle#enrollmentOpened}. */
    public boolean enrollmentOpened(EventSnapshot event, long now) {
        return StageLifecycle.enrollmentOpened(status(now), event.enrollmentDeadline(), now);
    }

    private boolean hasAnyScore() {
        return events != null && events.stream().anyMatch(EventSnapshot::hasAnyScore);
    }
}
