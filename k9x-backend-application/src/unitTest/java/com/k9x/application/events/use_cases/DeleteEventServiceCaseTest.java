package com.k9x.application.events.use_cases;

import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.port.DeleteObdxEventPersistencePort;
import com.k9x.application.events.obdx.use_cases.port.GetEventPersistencePort;
import com.k9x.application.events.use_cases.DeleteEventServiceCase;
import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteEventServiceCaseTest {

    @Mock
    private GetEventPersistencePort getEventPersistencePort;

    @Mock
    private GetStagePersistencePort getStagePersistencePort;

    @Mock
    private DeleteObdxEventPersistencePort deleteObdxEventPersistencePort;

    private DeleteEventServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new DeleteEventServiceCase(getEventPersistencePort, getStagePersistencePort, deleteObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.deleteEvent("event-1", "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getEventPersistencePort, getStagePersistencePort, deleteObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getEventPersistencePort.getEvent("event-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.deleteEvent("event-1", "user-1", true))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(getStagePersistencePort, deleteObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_event_is_deleted() {
        Event event = new Event("event-1", null, null, "Event 1", "stage-1", "user-1", 0L, 0L, 1700000000000L, ObdxAvgMethod.MID_AVG);
        when(getEventPersistencePort.getEvent("event-1")).thenReturn(event);

        assertThatThrownBy(() -> serviceCase.deleteEvent("event-1", "user-1", true))
                .isInstanceOf(EventAlreadyDeletedException.class);

        verifyNoInteractions(getStagePersistencePort, deleteObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_stage_is_deleted() {
        Event event = new Event("event-1", null, null, "Event 1", "stage-1", "user-1", 0L, 0L, null, ObdxAvgMethod.MID_AVG);
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE, 0L, 0L, 1700000000000L);
        when(getEventPersistencePort.getEvent("event-1")).thenReturn(event);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);

        assertThatThrownBy(() -> serviceCase.deleteEvent("event-1", "user-1", true))
                .isInstanceOf(StageAlreadyDeletedException.class);

        verifyNoInteractions(deleteObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_stage_creator() {
        Event event = new Event("event-1", null, null, "Event 1", "stage-1", "user-1", 0L, 0L, null, ObdxAvgMethod.MID_AVG);
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "other-user", Long.MAX_VALUE, 0L, 0L, null);
        when(getEventPersistencePort.getEvent("event-1")).thenReturn(event);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);

        assertThatThrownBy(() -> serviceCase.deleteEvent("event-1", "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(deleteObdxEventPersistencePort);
    }

    @Test
    void deletes_event_when_all_validations_pass() {
        Event event = new Event("event-1", null, null, "Event 1", "stage-1", "user-1", 0L, 0L, null, ObdxAvgMethod.MID_AVG);
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE, 0L, 0L, null);
        when(getEventPersistencePort.getEvent("event-1")).thenReturn(event);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);

        serviceCase.deleteEvent("event-1", "user-1", true);

        verify(deleteObdxEventPersistencePort).deleteEvent(eq("event-1"), anyLong());
    }
}
