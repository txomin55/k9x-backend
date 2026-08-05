package com.k9x.domain.subscriptions;

import com.k9x.domain.subscriptions.exceptions.SubscriptionKindNotSupportedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SubscriptionKindTest {

    @Test
    void resolves_the_kind_ignoring_case_and_surrounding_blanks() {
        assertEquals(SubscriptionKind.EVENT, SubscriptionKind.of(" event "));
        assertEquals(SubscriptionKind.EVENT, SubscriptionKind.of("EVENT"));
    }

    @Test
    void throws_exception_when_the_kind_is_unknown() {
        assertThrows(SubscriptionKindNotSupportedException.class, () -> SubscriptionKind.of("STAGE"));
    }

    @Test
    void throws_exception_when_the_kind_is_missing() {
        assertThrows(SubscriptionKindNotSupportedException.class, () -> SubscriptionKind.of(null));
        assertThrows(SubscriptionKindNotSupportedException.class, () -> SubscriptionKind.of("  "));
    }
}
