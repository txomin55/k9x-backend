package com.k9x.domain.events.status;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventCompetitorStatusTest {

    @Test
    void not_competing_when_flagged_not_competing_regardless_of_verified() {
        assertEquals(EventCompetitorStatus.NOT_COMPETING, EventCompetitorStatus.of(true, true));
        assertEquals(EventCompetitorStatus.NOT_COMPETING, EventCompetitorStatus.of(true, false));
        assertEquals(EventCompetitorStatus.NOT_COMPETING, EventCompetitorStatus.of(true, null));
    }

    @Test
    void enrolled_when_competing_and_verified() {
        assertEquals(EventCompetitorStatus.ENROLLED, EventCompetitorStatus.of(false, true));
    }

    @Test
    void pending_enroll_accept_when_competing_and_not_verified() {
        assertEquals(EventCompetitorStatus.PENDING_ENROLL_ACCEPT, EventCompetitorStatus.of(false, false));
    }

    @Test
    void pending_enroll_accept_when_competing_and_verified_is_null() {
        assertEquals(EventCompetitorStatus.PENDING_ENROLL_ACCEPT, EventCompetitorStatus.of(false, null));
    }
}
