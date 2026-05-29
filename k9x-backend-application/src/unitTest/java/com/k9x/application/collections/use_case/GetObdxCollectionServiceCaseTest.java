package com.k9x.application.collections.use_case;

import com.k9x.application.collections.port.GetObdxCollectionCompetitorsPersistencePort;
import com.k9x.application.collections.port.GetObdxCollectionEventJudgesPersistencePort;
import com.k9x.application.collections.port.GetObdxCollectionExercisesPersistencePort;
import com.k9x.application.collections.port.GetObdxCollectionScoresPersistencePort;
import com.k9x.application.collections.use_case.dto.FetchCollectionCompetitorDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionDetailDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionJudgeWithCollectorDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionScoreDTO;
import com.k9x.application.disciplines.obdx.port.GetObdxConfigurationAllowedValuesPort;
import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.exceptions.UserNotCollectorException;
import com.k9x.application.events.obdx.port.GetObdxEventPersistencePort;
import com.k9x.application.stages.exceptions.StageExpiredException;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.aggregates.events.obdx.ObdxEvent;
import com.k9x.domain.aggregates.stages.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetObdxCollectionServiceCaseTest {

    private static final ObdxEvent ACTIVE_EVENT = new ObdxEvent("event-1", "config-1", "Event A", "stage-1", "creator@test.com", 0L, 0L, null, ObdxAvgMethod.MID_AVG);
    private static final Stage ACTIVE_STAGE = new Stage("stage-1", "Stage A", "comp-1", "user-1", Long.MAX_VALUE, 0L, 0L, null);
    @Mock
    private GetObdxEventPersistencePort getObdxEventPersistencePort;
    @Mock
    private GetStagePersistencePort getStagePersistencePort;
    @Mock
    private GetObdxCollectionEventJudgesPersistencePort getObdxCollectionEventJudgesPersistencePort;
    @Mock
    private GetObdxCollectionCompetitorsPersistencePort getObdxCollectionCompetitorsPersistencePort;
    @Mock
    private GetObdxCollectionExercisesPersistencePort getObdxCollectionExercisesPersistencePort;
    @Mock
    private GetObdxCollectionScoresPersistencePort getObdxCollectionScoresPersistencePort;
    @Mock
    private GetObdxConfigurationAllowedValuesPort getObdxConfigurationAllowedValuesPort;
    private GetObdxCollectionServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetObdxCollectionServiceCase(
                getObdxEventPersistencePort,
                getStagePersistencePort,
                getObdxCollectionEventJudgesPersistencePort,
                getObdxCollectionCompetitorsPersistencePort,
                getObdxCollectionExercisesPersistencePort,
                getObdxCollectionScoresPersistencePort,
                getObdxConfigurationAllowedValuesPort);
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getObdxEventPersistencePort.getEvent("event-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.getCollection("event-1", "user@test.com"))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(getStagePersistencePort, getObdxCollectionEventJudgesPersistencePort,
                getObdxCollectionCompetitorsPersistencePort, getObdxCollectionExercisesPersistencePort,
                getObdxCollectionScoresPersistencePort, getObdxConfigurationAllowedValuesPort);
    }

    @Test
    void throws_exception_when_event_is_deleted() {
        ObdxEvent deletedEvent = new ObdxEvent("event-1", "config-1", "Event A", "stage-1", "creator@test.com", 0L, 0L, 1700000000000L, ObdxAvgMethod.MID_AVG);
        when(getObdxEventPersistencePort.getEvent("event-1")).thenReturn(deletedEvent);

        assertThatThrownBy(() -> serviceCase.getCollection("event-1", "user@test.com"))
                .isInstanceOf(EventAlreadyDeletedException.class);

        verifyNoInteractions(getStagePersistencePort, getObdxCollectionEventJudgesPersistencePort,
                getObdxCollectionCompetitorsPersistencePort, getObdxCollectionExercisesPersistencePort,
                getObdxCollectionScoresPersistencePort, getObdxConfigurationAllowedValuesPort);
    }

    @Test
    void throws_exception_when_stage_is_expired() {
        Stage expiredStage = new Stage("stage-1", "Stage A", "comp-1", "user-1", 1L, 0L, 0L, null);
        when(getObdxEventPersistencePort.getEvent("event-1")).thenReturn(ACTIVE_EVENT);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(expiredStage);

        assertThatThrownBy(() -> serviceCase.getCollection("event-1", "user@test.com"))
                .isInstanceOf(StageExpiredException.class);

        verifyNoInteractions(getObdxCollectionEventJudgesPersistencePort, getObdxCollectionCompetitorsPersistencePort,
                getObdxCollectionExercisesPersistencePort, getObdxCollectionScoresPersistencePort,
                getObdxConfigurationAllowedValuesPort);
    }

    @Test
    void throws_exception_when_user_is_not_collector_and_not_creator() {
        List<FetchCollectionJudgeWithCollectorDTO> judges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "other@test.com")
        );
        when(getObdxEventPersistencePort.getEvent("event-1")).thenReturn(ACTIVE_EVENT);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(ACTIVE_STAGE);
        when(getObdxCollectionEventJudgesPersistencePort.getJudges("event-1")).thenReturn(judges);

        assertThatThrownBy(() -> serviceCase.getCollection("event-1", "notcollector@test.com"))
                .isInstanceOf(UserNotCollectorException.class);
    }

    @Test
    void returns_all_judges_when_user_is_creator() {
        List<FetchCollectionJudgeWithCollectorDTO> judges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "collector1@test.com"),
                new FetchCollectionJudgeWithCollectorDTO("judge-2", "Judge Two", "collector2@test.com")
        );
        when(getObdxEventPersistencePort.getEvent("event-1")).thenReturn(ACTIVE_EVENT);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(ACTIVE_STAGE);
        when(getObdxCollectionEventJudgesPersistencePort.getJudges("event-1")).thenReturn(judges);
        when(getObdxCollectionCompetitorsPersistencePort.getCompetitors("event-1")).thenReturn(List.of());
        when(getObdxCollectionExercisesPersistencePort.getExercises("event-1")).thenReturn(List.of());
        when(getObdxCollectionScoresPersistencePort.getScores("event-1")).thenReturn(List.of());
        when(getObdxConfigurationAllowedValuesPort.getAllowedValues("config-1")).thenReturn(List.of(new BigDecimal("7.5")));

        FetchCollectionDetailDTO result = serviceCase.getCollection("event-1", "creator@test.com");

        assertThat(result.judges()).hasSize(2);
        assertThat(result.judges()).containsExactlyElementsOf(judges);
    }

    @Test
    void returns_only_user_judges_when_user_is_collector() {
        List<FetchCollectionJudgeWithCollectorDTO> judges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "collector@test.com"),
                new FetchCollectionJudgeWithCollectorDTO("judge-2", "Judge Two", "other@test.com")
        );
        when(getObdxEventPersistencePort.getEvent("event-1")).thenReturn(ACTIVE_EVENT);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(ACTIVE_STAGE);
        when(getObdxCollectionEventJudgesPersistencePort.getJudges("event-1")).thenReturn(judges);
        when(getObdxCollectionCompetitorsPersistencePort.getCompetitors("event-1")).thenReturn(List.of());
        when(getObdxCollectionExercisesPersistencePort.getExercises("event-1")).thenReturn(List.of());
        when(getObdxCollectionScoresPersistencePort.getScores("event-1")).thenReturn(List.of());
        when(getObdxConfigurationAllowedValuesPort.getAllowedValues("config-1")).thenReturn(List.of());

        FetchCollectionDetailDTO result = serviceCase.getCollection("event-1", "collector@test.com");

        assertThat(result.judges()).hasSize(1);
        assertThat(result.judges().getFirst().judgeId()).isEqualTo("judge-1");
    }

    @Test
    void sets_enrolled_status_on_competitors() {
        List<FetchCollectionJudgeWithCollectorDTO> judges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "collector@test.com")
        );
        List<FetchCollectionCompetitorDTO> rawCompetitors = List.of(
                new FetchCollectionCompetitorDTO("dog-1", "Rex", "ID-001", "owner@test.com", "Team A", "ES", (short) 1, true, null)
        );
        when(getObdxEventPersistencePort.getEvent("event-1")).thenReturn(ACTIVE_EVENT);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(ACTIVE_STAGE);
        when(getObdxCollectionEventJudgesPersistencePort.getJudges("event-1")).thenReturn(judges);
        when(getObdxCollectionCompetitorsPersistencePort.getCompetitors("event-1")).thenReturn(rawCompetitors);
        when(getObdxCollectionExercisesPersistencePort.getExercises("event-1")).thenReturn(List.of());
        when(getObdxCollectionScoresPersistencePort.getScores("event-1")).thenReturn(List.of());
        when(getObdxConfigurationAllowedValuesPort.getAllowedValues("config-1")).thenReturn(List.of());

        FetchCollectionDetailDTO result = serviceCase.getCollection("event-1", "collector@test.com");

        assertThat(result.competitors()).hasSize(1);
        assertThat(result.competitors().getFirst().status()).isEqualTo("ENROLLED");
    }

    @Test
    void filters_scores_by_visible_judge_ids() {
        List<FetchCollectionJudgeWithCollectorDTO> judges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "collector@test.com")
        );
        List<FetchCollectionScoreDTO> scores = List.of(
                new FetchCollectionScoreDTO("dog-1", "ex-1", "judge-1", new BigDecimal("7.5")),
                new FetchCollectionScoreDTO("dog-1", "ex-1", "judge-2", new BigDecimal("8.0"))
        );
        when(getObdxEventPersistencePort.getEvent("event-1")).thenReturn(ACTIVE_EVENT);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(ACTIVE_STAGE);
        when(getObdxCollectionEventJudgesPersistencePort.getJudges("event-1")).thenReturn(judges);
        when(getObdxCollectionCompetitorsPersistencePort.getCompetitors("event-1")).thenReturn(List.of());
        when(getObdxCollectionExercisesPersistencePort.getExercises("event-1")).thenReturn(List.of());
        when(getObdxCollectionScoresPersistencePort.getScores("event-1")).thenReturn(scores);
        when(getObdxConfigurationAllowedValuesPort.getAllowedValues("config-1")).thenReturn(List.of());

        FetchCollectionDetailDTO result = serviceCase.getCollection("event-1", "collector@test.com");

        assertThat(result.scores()).hasSize(1);
        assertThat(result.scores().getFirst().judgeId()).isEqualTo("judge-1");
    }
}
