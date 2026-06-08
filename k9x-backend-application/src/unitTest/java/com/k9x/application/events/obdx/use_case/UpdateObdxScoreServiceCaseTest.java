package com.k9x.application.events.obdx.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.disciplines.obdx.port.GetObdxExerciseAllowedValuesPort;
import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.exceptions.ObdxScoreNotAllowedException;
import com.k9x.application.events.obdx.exceptions.ObdxUserNotCollectorException;
import com.k9x.application.events.obdx.port.GetObdxEventCollectorPersistencePort;
import com.k9x.application.events.obdx.port.UpdateObdxScorePersistencePort;
import com.k9x.application.events.obdx.use_case.command.UpdateObdxScoreCommand;
import com.k9x.application.stages.exceptions.StageExpiredException;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.aggregates.events.Event;
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

    private static final UpdateObdxScoreCommand COMMAND = new UpdateObdxScoreCommand(
            "judge-1", "OBDX_FCI_GRADE_3.1_V0", "dog-1", new BigDecimal("7.5"));
    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;
    @Mock
    private GetObdxEventCollectorPersistencePort getObdxEventCollectorPersistencePort;
    @Mock
    private GetObdxExerciseAllowedValuesPort getObdxExerciseAllowedValuesPort;
    @Mock
    private UpdateObdxScorePersistencePort updateObdxScorePersistencePort;
    private UpdateObdxScoreServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new UpdateObdxScoreServiceCase(getCompetitionPersistencePort,
                getObdxEventCollectorPersistencePort, getObdxExerciseAllowedValuesPort, updateObdxScorePersistencePort);
    }

    private Event event(Long deletedAt) {
        return new Event("event-1", null, null, "Event 1", "stage-1", "user-1", 0L, 0L, deletedAt,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of());
    }

    private Competition competition(Event event, long dateTo) {
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "user-1", 0L, dateTo, 0L, 0L, null, List.of(event));
        return new Competition("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                0L, 0L, null, List.of(stage));
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateScore("event-1", COMMAND, "user@k9x.io"))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(getObdxEventCollectorPersistencePort,
                getObdxExerciseAllowedValuesPort, updateObdxScorePersistencePort);
    }

    @Test
    void throws_exception_when_event_is_deleted() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(event(1700000000000L), Long.MAX_VALUE));

        assertThatThrownBy(() -> serviceCase.updateScore("event-1", COMMAND, "user@k9x.io"))
                .isInstanceOf(EventAlreadyDeletedException.class);

        verifyNoInteractions(getObdxEventCollectorPersistencePort,
                getObdxExerciseAllowedValuesPort, updateObdxScorePersistencePort);
    }

    @Test
    void throws_exception_when_stage_is_expired() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(event(null), 1L));

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
                .isInstanceOf(ObdxUserNotCollectorException.class);

        verifyNoInteractions(getObdxExerciseAllowedValuesPort, updateObdxScorePersistencePort);
    }

    @Test
    void throws_exception_when_no_collector_assigned() {
        givenActiveEventAndStage();
        when(getObdxEventCollectorPersistencePort.getCollectorId("event-1", "judge-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateScore("event-1", COMMAND, "user@k9x.io"))
                .isInstanceOf(ObdxUserNotCollectorException.class);
    }

    @Test
    void throws_exception_when_score_is_not_allowed() {
        givenActiveEventAndStage();
        when(getObdxEventCollectorPersistencePort.getCollectorId("event-1", "judge-1")).thenReturn("user@k9x.io");
        when(getObdxExerciseAllowedValuesPort.getAllowedValues("OBDX_FCI_GRADE_3.1_V0"))
                .thenReturn(List.of(new BigDecimal("5"), new BigDecimal("6")));

        assertThatThrownBy(() -> serviceCase.updateScore("event-1", COMMAND, "user@k9x.io"))
                .isInstanceOf(ObdxScoreNotAllowedException.class);

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
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(event(null), Long.MAX_VALUE));
    }
}
