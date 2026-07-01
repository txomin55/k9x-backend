package com.k9x.domain.stages.aggregates;

import com.k9x.domain.stages.status.StageStatus;

import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.status.EventStatus;
import com.k9x.domain.shared.UtcDates;

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

    /**
     * Lifecycle status based on the UTC calendar day and the status of its events:
     * FINISHED once the day after {@code dateTo} has arrived, STARTED while any event is started,
     * TO_START on the {@code dateFrom} day, otherwise CREATED.
     */
    public StageStatus status(long now) {
        if (deletedAt != null) {
            return StageStatus.DELETED;
        }
        if (UtcDates.isAfterUtcDay(now, dateTo)) {
            return StageStatus.FINISHED;
        }
        if (hasStartedEvent()) {
            return StageStatus.STARTED;
        }
        if (UtcDates.isSameUtcDay(now, dateFrom)) {
            return StageStatus.TO_START;
        }
        return StageStatus.CREATED;
    }

    /**
     * Whether enrollment is open for the given event within this stage. Enrollment closes once the stage
     * is under way: a TO_START or STARTED stage never accepts enrollments regardless of the event's own
     * deadline. Otherwise the event's deadline decides.
     */
    public boolean enrollmentOpened(EventSnapshot event, long now) {
        StageStatus status = status(now);
        if (status == StageStatus.TO_START || status == StageStatus.STARTED) {
            return false;
        }
        return event.enrollmentOpened(now);
    }

    private boolean hasStartedEvent() {
        return events != null && events.stream().anyMatch(e -> e.status() == EventStatus.STARTED);
    }
}
