package com.k9x.domain.aggregates.stages;

import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.events.EventStatus;
import com.k9x.domain.shared.UtcDates;

import java.util.List;

public record Stage(
        String id,
        String name,
        String competitionId,
        String creator,
        long dateFrom,
        long dateTo,
        long lastUpdate,
        long createdAt,
        Long deletedAt,
        List<Event> events
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

    private boolean hasStartedEvent() {
        return events != null && events.stream().anyMatch(e -> e.status() == EventStatus.STARTED);
    }
}
