package com.k9x.application.events.obdx.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.disciplines.obdx.port.GetObdxExerciseAllowedValuesPort;
import com.k9x.application.events.obdx.exceptions.ObdxScoreNotAllowedException;
import com.k9x.application.events.obdx.exceptions.ObdxUserNotCollectorException;
import com.k9x.application.events.obdx.port.GetObdxEventCollectorPersistencePort;
import com.k9x.application.events.obdx.use_case.command.UpdateObdxScoreCommand;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.competitions.aggregates.CompetitionSource;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.valueobjects.EventExercise;
import com.k9x.domain.events.valueobjects.Score;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import com.k9x.domain.events.exceptions.CompetitorDisqualifiedException;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.domain.events.exceptions.ExerciseJudgeNotAssignedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateObdxScoreServiceCaseTest {

    private static final UpdateObdxScoreCommand COMMAND = new UpdateObdxScoreCommand(
            "judge-1", "OBDX.FCI_GRADE_3.1_V0", "dog-1", new BigDecimal("7.5"));

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;
    @Mock
    private GetObdxEventCollectorPersistencePort getObdxEventCollectorPersistencePort;
    @Mock
    private GetObdxExerciseAllowedValuesPort getObdxExerciseAllowedValuesPort;
    @Mock
    private SaveCompetitionPersistencePort saveCompetitionPersistencePort;
    private UpdateObdxScoreServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new UpdateObdxScoreServiceCase(getCompetitionPersistencePort,
                getObdxEventCollectorPersistencePort, getObdxExerciseAllowedValuesPort, saveCompetitionPersistencePort);
    }

    private static final List<EventExercise> EXERCISES = List.of(
            new EventExercise("OBDX.FCI_GRADE_3.1_V0", (short) 1, List.of(), List.of("judge-1")));

    private CompetitionSnapshot competition() {
        EventSnapshot event = new EventSnapshot("event-1", null, null, "Event 1", "stage-1", "user-1", null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), EXERCISES, List.of(), List.of(), List.of(), null, null, null);
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "user-1", 0L, Long.MAX_VALUE, 0L, 0L, null,
                List.of(event));
        return new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                CompetitionSource.API, null, 0L, 0L, null, List.of(stage));
    }

    private CompetitionSnapshot competitionWithDisqualifiedDog() {
        EventSnapshot event = new EventSnapshot("event-1", null, null, "Event 1", "stage-1", "user-1", null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), EXERCISES, List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", null, 0L, 1000L),
                        new Score("ex-2", "judge-1", "dog-1", null, 0L, 2000L)), List.of(), null, null, null);
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "user-1", 0L, Long.MAX_VALUE, 0L, 0L, null,
                List.of(event));
        return new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                CompetitionSource.API, null, 0L, 0L, null, List.of(stage));
    }

    private CompetitionSnapshot competitionWithExerciseJudgedByAnother() {
        EventSnapshot event = new EventSnapshot("event-1", null, null, "Event 1", "stage-1", "user-1", null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(),
                List.of(new EventExercise("OBDX.FCI_GRADE_3.1_V0", (short) 1, List.of(), List.of("judge-2"))),
                List.of(), List.of(), List.of(), null, null, null);
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "user-1", 0L, Long.MAX_VALUE, 0L, 0L, null,
                List.of(event));
        return new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                CompetitionSource.API, null, 0L, 0L, null, List.of(stage));
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateScore("event-1", COMMAND, "user@k9x.io"))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(getObdxEventCollectorPersistencePort, getObdxExerciseAllowedValuesPort,
                saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_neither_collector_nor_event_creator() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition());
        when(getObdxEventCollectorPersistencePort.getCollectorId("event-1", "judge-1")).thenReturn("other@k9x.io");

        assertThatThrownBy(() -> serviceCase.updateScore("event-1", COMMAND, "user@k9x.io"))
                .isInstanceOf(ObdxUserNotCollectorException.class);

        verifyNoInteractions(getObdxExerciseAllowedValuesPort, saveCompetitionPersistencePort);
    }

    @Test
    void saves_aggregate_when_user_is_event_creator_even_though_not_collector() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition());
        when(getObdxExerciseAllowedValuesPort.getAllowedValues("OBDX.FCI_GRADE_3.1_V0"))
                .thenReturn(List.of(new BigDecimal("7.5")));

        serviceCase.updateScore("event-1", COMMAND, "user-1");

        verify(saveCompetitionPersistencePort).save(any());
        verifyNoInteractions(getObdxEventCollectorPersistencePort);
    }

    @Test
    void throws_exception_when_score_is_not_allowed() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition());
        when(getObdxEventCollectorPersistencePort.getCollectorId("event-1", "judge-1")).thenReturn("user@k9x.io");
        when(getObdxExerciseAllowedValuesPort.getAllowedValues("OBDX.FCI_GRADE_3.1_V0"))
                .thenReturn(List.of(new BigDecimal("5"), new BigDecimal("6")));

        assertThatThrownBy(() -> serviceCase.updateScore("event-1", COMMAND, "user@k9x.io"))
                .isInstanceOf(ObdxScoreNotAllowedException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_competitor_is_disqualified() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getObdxEventCollectorPersistencePort.getCollectorId("event-1", "judge-1")).thenReturn("user@k9x.io");
        when(getObdxExerciseAllowedValuesPort.getAllowedValues("OBDX.FCI_GRADE_3.1_V0"))
                .thenReturn(List.of(new BigDecimal("7.5")));
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competitionWithDisqualifiedDog());

        assertThatThrownBy(() -> serviceCase.updateScore("event-1", COMMAND, "user@k9x.io"))
                .isInstanceOf(CompetitorDisqualifiedException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_judge_is_not_assigned_to_the_exercise() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getObdxEventCollectorPersistencePort.getCollectorId("event-1", "judge-1")).thenReturn("user@k9x.io");
        when(getObdxExerciseAllowedValuesPort.getAllowedValues("OBDX.FCI_GRADE_3.1_V0"))
                .thenReturn(List.of(new BigDecimal("7.5")));
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competitionWithExerciseJudgedByAnother());

        assertThatThrownBy(() -> serviceCase.updateScore("event-1", COMMAND, "user@k9x.io"))
                .isInstanceOf(ExerciseJudgeNotAssignedException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void saves_aggregate_when_all_validations_pass() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getObdxEventCollectorPersistencePort.getCollectorId("event-1", "judge-1")).thenReturn("user@k9x.io");
        when(getObdxExerciseAllowedValuesPort.getAllowedValues("OBDX.FCI_GRADE_3.1_V0"))
                .thenReturn(List.of(new BigDecimal("7.5")));
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition());

        serviceCase.updateScore("event-1", COMMAND, "user@k9x.io");

        verify(saveCompetitionPersistencePort).save(any());
    }
}
