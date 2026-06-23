package com.k9x.domain.events.status;

public enum EventCompetitorStatus {
    ENROLLED,
    PENDING_ENROLL_ACCEPT,
    NOT_COMPETING;

    public static EventCompetitorStatus of(boolean notCompeting) {
        return notCompeting ? NOT_COMPETING : ENROLLED;
    }
}
