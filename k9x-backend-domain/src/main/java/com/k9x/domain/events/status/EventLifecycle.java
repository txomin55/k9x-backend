package com.k9x.domain.events.status;

import com.k9x.domain.shared.UtcDates;

import java.util.function.BooleanSupplier;

/**
 * The event lifecycle rules as pure functions over the few facts they depend on, so a read model can resolve
 * the status without hydrating the event: {@link com.k9x.domain.events.aggregates.EventSnapshot} delegates here
 * and so does any query projection. The facts that are expensive to know (every competitor settled, any score
 * recorded) are suppliers, evaluated only when the cheaper date rule has not already decided.
 */
public final class EventLifecycle {

    private EventLifecycle() {
    }

    /**
     * "Pooling" is only a front-end label, so the status comes from the recorded scores: an event is FINISHED
     * once its stage's {@code dateTo} day has passed or once every competitor is settled, STARTED once any score
     * has been taken, otherwise CREATED.
     */
    public static EventStatus status(Long deletedAt, long now, long stageDateTo,
                                     BooleanSupplier allCompetitorsSettled, BooleanSupplier hasAnyScore) {
        if (deletedAt != null) {
            return EventStatus.DELETED;
        }
        if (UtcDates.isAfterUtcDay(now, stageDateTo)) {
            return EventStatus.FINISHED;
        }
        if (allCompetitorsSettled.getAsBoolean()) {
            return EventStatus.FINISHED;
        }
        if (hasAnyScore.getAsBoolean()) {
            return EventStatus.STARTED;
        }
        return EventStatus.CREATED;
    }

    /**
     * Whether the event's own deadline still accepts enrollments: an event with no deadline set never does (a
     * deadline must be configured first), otherwise until the deadline's UTC day has passed.
     */
    public static boolean enrollmentOpened(Long enrollmentDeadline, long now) {
        return enrollmentDeadline != null && !UtcDates.isAfterUtcDay(now, enrollmentDeadline);
    }
}
