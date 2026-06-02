package com.k9x.application.events.use_cases;

import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.port.EnrollObdxEventPersistencePort;
import com.k9x.application.events.obdx.use_cases.command.EnrollObdxEventCommand;
import com.k9x.application.events.obdx.use_cases.port.GetEventPersistencePort;
import com.k9x.application.events.use_cases.EnrollEventServiceCase;
import com.k9x.application.stages.exceptions.StageExpiredException;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.stages.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollEventServiceCaseTest {

    @Mock
    private GetEventPersistencePort getEventPersistencePort;

    @Mock
    private GetStagePersistencePort getStagePersistencePort;

    @Mock
    private EnrollObdxEventPersistencePort enrollObdxEventPersistencePort;

    private EnrollEventServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new EnrollEventServiceCase(getEventPersistencePort, getStagePersistencePort, enrollObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getEventPersistencePort.getEvent("event-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.enrollEvent("event-1", new EnrollObdxEventCommand("dog-1")))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(getStagePersistencePort, enrollObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_event_is_deleted() {
        Event event = new Event("event-1", null, null, "Event 1", "stage-1", "user-1", 0L, 0L, 1700000000000L, ObdxAvgMethod.MID_AVG);
        when(getEventPersistencePort.getEvent("event-1")).thenReturn(event);

        assertThatThrownBy(() -> serviceCase.enrollEvent("event-1", new EnrollObdxEventCommand("dog-1")))
                .isInstanceOf(EventAlreadyDeletedException.class);

        verifyNoInteractions(getStagePersistencePort, enrollObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_stage_is_expired() {
        Event event = new Event("event-1", null, null, "Event 1", "stage-1", "user-1", 0L, 0L, null, ObdxAvgMethod.MID_AVG);
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "user-1", 1L, 0L, 0L, null);
        when(getEventPersistencePort.getEvent("event-1")).thenReturn(event);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);

        assertThatThrownBy(() -> serviceCase.enrollEvent("event-1", new EnrollObdxEventCommand("dog-1")))
                .isInstanceOf(StageExpiredException.class);

        verifyNoInteractions(enrollObdxEventPersistencePort);
    }

    @Test
    void enrolls_event_when_all_validations_pass() {
        Event event = new Event("event-1", null, null, "Event 1", "stage-1", "user-1", 0L, 0L, null, ObdxAvgMethod.MID_AVG);
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE, 0L, 0L, null);
        when(getEventPersistencePort.getEvent("event-1")).thenReturn(event);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);

        serviceCase.enrollEvent("event-1", new EnrollObdxEventCommand("dog-1"));

        verify(enrollObdxEventPersistencePort).enrollEvent(eq("event-1"), any());
    }
}
