package com.k9x.domain.stages.status;

import com.k9x.domain.events.status.EventLifecycle;
import com.k9x.domain.events.status.EventStatus;
import com.k9x.domain.shared.UtcDates;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * The stage lifecycle rules as pure functions, shared by {@link com.k9x.domain.stages.aggregates.StageSnapshot}
 * and the query projections that list stages without hydrating them. The per-event facts are suppliers, so they
 * are only computed when the date rules have not already decided.
 */
public final class StageLifecycle {

    private StageLifecycle() {
    }

    /**
     * Based on the UTC calendar day and the scores recorded on its events: FINISHED once the day after
     * {@code dateTo} has arrived, or once every event is itself FINISHED; STARTED once any competitor has a
     * score; TO_START while {@code now} falls within [{@code dateFrom}, {@code dateTo}] and no score has been
     * recorded yet, otherwise CREATED. {@code eventStatuses} covers every event of the stage, deleted ones
     * included: a deleted event is not FINISHED, so it keeps the stage from finishing early.
     */
    public static StageStatus status(Long deletedAt, long now, long dateFrom, long dateTo,
                                     Supplier<List<EventStatus>> eventStatuses, BooleanSupplier anyEventScored) {
        if (deletedAt != null) {
            return StageStatus.DELETED;
        }
        if (UtcDates.isAfterUtcDay(now, dateTo)) {
            return StageStatus.FINISHED;
        }
        List<EventStatus> statuses = eventStatuses.get();
        if (!statuses.isEmpty() && statuses.stream().allMatch(s -> s == EventStatus.FINISHED)) {
            return StageStatus.FINISHED;
        }
        if (anyEventScored.getAsBoolean()) {
            return StageStatus.STARTED;
        }
        if (!UtcDates.isBeforeUtcDay(now, dateFrom)) {
            return StageStatus.TO_START;
        }
        return StageStatus.CREATED;
    }

    /**
     * Whether an event of a stage in {@code stageStatus} accepts enrollments: never once the stage is under way
     * (TO_START or STARTED), whatever the event's own deadline; otherwise the deadline decides.
     */
    public static boolean enrollmentOpened(StageStatus stageStatus, Long eventEnrollmentDeadline, long now) {
        if (stageStatus == StageStatus.TO_START || stageStatus == StageStatus.STARTED) {
            return false;
        }
        return EventLifecycle.enrollmentOpened(eventEnrollmentDeadline, now);
    }
}
