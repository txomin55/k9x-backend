package com.k9x.domain.events.status;

public enum EventCompetitorStatus {
    ENROLLED,
    PENDING_ENROLL_ACCEPT,
    NOT_COMPETING;

    public static EventCompetitorStatus of(boolean notCompeting, Boolean verified) {
        if (notCompeting) {
            return NOT_COMPETING;
        }
        return Boolean.TRUE.equals(verified) ? ENROLLED : PENDING_ENROLL_ACCEPT;
    }
}
