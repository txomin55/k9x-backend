package com.k9x.application.events.obdx.use_case;

import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventConfigurationIdRequiredException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.port.GetObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.port.UpdateObdxEventPersistencePort;
import com.k9x.application.events.obdx.use_case.dto.ObdxClassificationConfigDTO;
import com.k9x.domain.aggregates.disciplines.ClassificationCacheEvictStrategy;
import com.k9x.application.events.obdx.use_case.command.UpdateObdxEventCommand;
import com.k9x.domain.aggregates.events.obdx.ObdxEvent;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateObdxEventServiceCaseTest {

    @Mock
    private GetObdxEventPersistencePort getObdxEventPersistencePort;

    @Mock
    private UpdateObdxEventPersistencePort updateObdxEventPersistencePort;

    @Mock
    private GetObdxClassificationConfigPort getObdxClassificationConfigPort;

    private UpdateObdxEventServiceCase serviceCase;

    private static final UpdateObdxEventCommand VALID_COMMAND = new UpdateObdxEventCommand(
            "Event 1", "config-1", List.of(), List.of(), List.of()
    );

    @BeforeEach
    void setUp() {
        serviceCase = new UpdateObdxEventServiceCase(getObdxEventPersistencePort, updateObdxEventPersistencePort, getObdxClassificationConfigPort);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", VALID_COMMAND, "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getObdxEventPersistencePort, updateObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_configuration_id_is_null() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", null, List.of(), List.of(), List.of());

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(EventConfigurationIdRequiredException.class);

        verifyNoInteractions(getObdxEventPersistencePort, updateObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_configuration_id_is_blank() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "  ", List.of(), List.of(), List.of());

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(EventConfigurationIdRequiredException.class);

        verifyNoInteractions(getObdxEventPersistencePort, updateObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getObdxEventPersistencePort.getEvent("event-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", VALID_COMMAND, "user-1", true))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(updateObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_event_is_deleted() {
        ObdxEvent event = new ObdxEvent("event-1", null, "Event 1", "stage-1", "user-1", 0L, 0L, 1700000000000L, ObdxAvgMethod.MID_AVG);
        when(getObdxEventPersistencePort.getEvent("event-1")).thenReturn(event);

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", VALID_COMMAND, "user-1", true))
                .isInstanceOf(EventAlreadyDeletedException.class);

        verifyNoInteractions(updateObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_event_creator() {
        ObdxEvent event = new ObdxEvent("event-1", null, "Event 1", "stage-1", "other-user", 0L, 0L, null, ObdxAvgMethod.MID_AVG);
        when(getObdxEventPersistencePort.getEvent("event-1")).thenReturn(event);

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", VALID_COMMAND, "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(updateObdxEventPersistencePort);
    }

    @Test
    void updates_event_when_all_validations_pass() {
        ObdxEvent event = new ObdxEvent("event-1", null, "Event 1", "stage-1", "user-1", 0L, 0L, null, ObdxAvgMethod.MID_AVG);
        ObdxClassificationConfigDTO config = new ObdxClassificationConfigDTO(ClassificationCacheEvictStrategy.OBDX, null, Map.of(), List.of(), List.of());
        when(getObdxEventPersistencePort.getEvent("event-1")).thenReturn(event);
        when(getObdxClassificationConfigPort.getConfig("config-1")).thenReturn(config);

        serviceCase.updateEvent("event-1", VALID_COMMAND, "user-1", true);

        verify(updateObdxEventPersistencePort).updateEvent(eq("event-1"), any());
    }
}
