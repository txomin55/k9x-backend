package com.k9x.application.notifications.use_case;

import com.k9x.application.notifications.port.MarkNotificationsSeenPersistencePort;
import com.k9x.application.notifications.use_case.command.MarkNotificationsSeenCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MarkNotificationsSeenServiceCaseTest {

    @Mock MarkNotificationsSeenPersistencePort markNotificationsSeenPersistencePort;
    private MarkNotificationsSeenServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new MarkNotificationsSeenServiceCase(markNotificationsSeenPersistencePort);
    }

    @Test
    void marks_the_users_notifications_as_seen() {
        serviceCase.markSeen(new MarkNotificationsSeenCommand(List.of("1", "2")), "creator-1");

        verify(markNotificationsSeenPersistencePort).markSeen("creator-1", List.of("1", "2"));
    }

    @Test
    void does_nothing_when_ids_are_empty() {
        serviceCase.markSeen(new MarkNotificationsSeenCommand(Collections.emptyList()), "creator-1");

        verifyNoInteractions(markNotificationsSeenPersistencePort);
    }

    @Test
    void does_nothing_when_ids_are_null() {
        serviceCase.markSeen(new MarkNotificationsSeenCommand(null), "creator-1");

        verifyNoInteractions(markNotificationsSeenPersistencePort);
    }
}
