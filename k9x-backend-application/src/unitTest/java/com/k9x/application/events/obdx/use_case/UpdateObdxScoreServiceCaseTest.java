package com.k9x.application.events.obdx.use_case;

import com.k9x.application.disciplines.obdx.port.GetObdxExerciseAllowedValuesPort;
import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.exceptions.ScoreNotAllowedException;
import com.k9x.application.events.obdx.exceptions.UserNotCollectorException;
import com.k9x.application.events.obdx.port.GetObdxEventCollectorPersistencePort;
import com.k9x.application.events.obdx.port.GetObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.UpdateObdxScorePersistencePort;
import com.k9x.application.events.obdx.use_case.command.UpdateObdxScoreCommand;
import com.k9x.application.stages.exceptions.StageExpiredException;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.domain.aggregates.events.obdx.ObdxEvent;
import com.k9x.domain.aggregates.stages.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateObdxScoreServiceCaseTest {

    @Mock private GetObdxEventPersistencePort getObdxEventPersistencePort;
    @Mock private GetStagePersistencePort getStagePersistencePort;
    @Mock private GetObdxEventCollectorPersistencePort getObdxEventCollectorPersistencePort;
    @Mock private GetObdxExerciseAllowedValuesPort getObdxExerciseAllowedValuesPort;
    @Mock private UpdateObdxScorePersistencePort updateObdxScorePersistencePort;

    private UpdateObdxScoreServiceCase serviceCase;

    private static final UpdateObdxScoreCommand COMMAND = new UpdateObdxScoreCommand(
            "judge-1", "OBDX_FCI_GRADE_3.1_V0", "dog-1", new BigDecimal("7.5"));

    @BeforeEach
    void setUp() {
        serviceCase = new UpdateObdxScoreServiceCase(getObdxEventPersistencePort, getStagePersistencePort,
                getObdxEventCollectorPersistencePort, getObdxExerciseAllowedValuesPort, updateObdxScorePersistencePort);
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getObdxEventPersistencePort.getEvent("event-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateScore("event-1", COMMAND, "user@k9x.io"))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(getStagePersistencePort, getObdxEventCollectorPersistencePort,
                getObdxExerciseAllowedValuesPort, updateObdxScorePersistencePort);
    }

    @Test
    void throws_exception_when_event_is_deleted() {
        ObdxEvent event = new ObdxEvent("event-1", null, "Event 1", "stage-1", "user-1", 0L, 0L, 1700000000000L);
        when(getObdxEventPersistencePort.getEvent("event-1")).thenReturn(event);

        assertThatThrownBy(() -> serviceCase.updateScore("event-1", COMMAND, "user@k9x.io"))
                .isInstanceOf(EventAlreadyDeletedException.class);

        verifyNoInteractions(getStagePersistencePort, getObdxEventCollectorPersistencePort,
                getObdxExerciseAllowedValuesPort, updateObdxScorePersistencePort);
    }

    @Test
    void throws_exception_when_stage_is_expired() {
        ObdxEvent event = new ObdxEvent("event-1", null, "Event 1", "stage-1", "user-1", 0L, 0L, null);
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "user-1", 1L, 0L, 0L, null);
        when(getObdxEventPersistencePort.getEvent("event-1")).thenReturn(event);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);

        assertThatThrownBy(() -> serviceCase.updateScore("event-1", COMMAND, "user@k9x.io"))
                .isInstanceOf(StageExpiredException.class);

        verifyNoInteractions(getObdxEventCollectorPersistencePort, getObdxExerciseAllowedValuesPort,
                updateObdxScorePersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_collector() {
        givenActiveEventAndStage();
        when(getObdxEventCollectorPersistencePort.getCollectorId("event-1", "judge-1")).thenReturn("other@k9x.io");

        assertThatThrownBy(() -> serviceCase.updateScore("event-1", COMMAND, "user@k9x.io"))
                .isInstanceOf(UserNotCollectorException.class);

        verifyNoInteractions(getObdxExerciseAllowedValuesPort, updateObdxScorePersistencePort);
    }

    @Test
    void throws_exception_when_no_collector_assigned() {
        givenActiveEventAndStage();
        when(getObdxEventCollectorPersistencePort.getCollectorId("event-1", "judge-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateScore("event-1", COMMAND, "user@k9x.io"))
                .isInstanceOf(UserNotCollectorException.class);
    }

    @Test
    void throws_exception_when_score_is_not_allowed() {
        givenActiveEventAndStage();
        when(getObdxEventCollectorPersistencePort.getCollectorId("event-1", "judge-1")).thenReturn("user@k9x.io");
        when(getObdxExerciseAllowedValuesPort.getAllowedValues("OBDX_FCI_GRADE_3.1_V0"))
                .thenReturn(List.of(new BigDecimal("5"), new BigDecimal("6")));

        assertThatThrownBy(() -> serviceCase.updateScore("event-1", COMMAND, "user@k9x.io"))
                .isInstanceOf(ScoreNotAllowedException.class);

        verifyNoInteractions(updateObdxScorePersistencePort);
    }

    @Test
    void updates_score_when_all_validations_pass() {
        givenActiveEventAndStage();
        when(getObdxEventCollectorPersistencePort.getCollectorId("event-1", "judge-1")).thenReturn("user@k9x.io");
        when(getObdxExerciseAllowedValuesPort.getAllowedValues("OBDX_FCI_GRADE_3.1_V0"))
                .thenReturn(List.of(new BigDecimal("7.5")));

        serviceCase.updateScore("event-1", COMMAND, "user@k9x.io");

        verify(updateObdxScorePersistencePort).updateScore(eq("event-1"), any());
    }

    private void givenActiveEventAndStage() {
        ObdxEvent event = new ObdxEvent("event-1", null, "Event 1", "stage-1", "user-1", 0L, 0L, null);
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE, 0L, 0L, null);
        when(getObdxEventPersistencePort.getEvent("event-1")).thenReturn(event);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);
    }
}
