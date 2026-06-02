package com.k9x.application.events.use_cases;

import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.dto.ConfigurationDTO;
import com.k9x.application.disciplines.use_case.dto.ConfigurationsDTO;
import com.k9x.application.disciplines.use_case.dto.ExerciseDTO;
import com.k9x.application.disciplines.use_case.dto.FederationInfoDTO;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.use_cases.port.GetEventPersistencePort;
import com.k9x.application.events.obdx.use_cases.port.GetObdxEventDataPersistencePort;
import com.k9x.application.events.obdx.use_cases.dto.FetchObdxEventCompetitorDTO;
import com.k9x.application.events.obdx.use_cases.dto.FetchObdxEventDataDTO;
import com.k9x.application.events.obdx.use_cases.dto.FetchObdxEventExerciseDTO;
import com.k9x.application.events.obdx.use_cases.dto.FetchObdxEventJudgeDTO;
import com.k9x.application.events.use_cases.GetEventServiceCase;
import com.k9x.application.events.use_cases.dto.FetchEventDetailDTO;
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

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetEventServiceCaseTest {

    @Mock
    private GetEventPersistencePort getEventPersistencePort;
    @Mock
    private GetStagePersistencePort getStagePersistencePort;
    @Mock
    private GetObdxEventDataPersistencePort getObdxEventDataPersistencePort;
    @Mock
    private GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort;

    private GetEventServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetEventServiceCase(getEventPersistencePort, getStagePersistencePort,
                getObdxEventDataPersistencePort, getObdxFederationsConfigurationsPort);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.getEvent("event-1", "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getEventPersistencePort, getStagePersistencePort, getObdxEventDataPersistencePort);
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getEventPersistencePort.getEvent("event-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.getEvent("event-1", "user-1", true))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(getStagePersistencePort, getObdxEventDataPersistencePort);
    }

    @Test
    void throws_exception_when_stage_is_deleted() {
        Event event = new Event("event-1", "cfg-1", "OBDX", "Event 1", "stage-1", "user-1", 0L, 0L, null, ObdxAvgMethod.MID_AVG);
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE, 0L, 0L, 1700000000000L);
        when(getEventPersistencePort.getEvent("event-1")).thenReturn(event);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);

        assertThatThrownBy(() -> serviceCase.getEvent("event-1", "user-1", true))
                .isInstanceOf(StageAlreadyDeletedException.class);
    }

    @Test
    void throws_exception_when_user_is_not_stage_creator() {
        Event event = new Event("event-1", "cfg-1", "OBDX", "Event 1", "stage-1", "user-1", 0L, 0L, null, ObdxAvgMethod.MID_AVG);
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "other-user", Long.MAX_VALUE, 0L, 0L, null);
        when(getEventPersistencePort.getEvent("event-1")).thenReturn(event);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);

        assertThatThrownBy(() -> serviceCase.getEvent("event-1", "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);
    }

    @Test
    void returns_obdx_event_detail_when_all_validations_pass() throws IOException {
        Event event = new Event("event-1", "cfg-1", "OBDX", "Event 1", "stage-1", "user-1", 0L, 0L, null, ObdxAvgMethod.MID_AVG);
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE, 0L, 0L, null);
        when(getEventPersistencePort.getEvent("event-1")).thenReturn(event);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);
        when(getObdxEventDataPersistencePort.getEventData("event-1")).thenReturn(new FetchObdxEventDataDTO(
                List.of(new FetchObdxEventCompetitorDTO("dog-1", "Rex", "id-1", "breed", "owner", "team", "ES", (short) 1, true, null)),
                List.of(new FetchObdxEventExerciseDTO("ex-1", (short) 1, List.of("tag-a", "tag-b"))),
                List.of(new FetchObdxEventJudgeDTO("judge-1", "Judge", "collector@k9x.com"))));
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenReturn(List.of(
                new ConfigurationsDTO(new FederationInfoDTO("fed-1", "Federation", "ES"),
                        List.of(new ConfigurationDTO("cfg-1", "Config 1", List.of(new ExerciseDTO("ex-1", "Exercise 1")))))));

        FetchEventDetailDTO result = serviceCase.getEvent("event-1", "user-1", true);

        assertThat(result.obdx()).isNotNull();
        assertThat(result.obdx().id()).isEqualTo("event-1");
        assertThat(result.obdx().discipline()).isEqualTo("OBDX");
        assertThat(result.competitors()).hasSize(1);
        assertThat(result.competitors().getFirst().status()).isEqualTo("ENROLLED");
        assertThat(result.judges()).hasSize(1);
        assertThat(result.exercises()).hasSize(1);
        assertThat(result.exercises().getFirst().name()).isEqualTo("Exercise 1");
        assertThat(result.exercises().getFirst().position()).isEqualTo(1);
        assertThat(result.exercises().getFirst().tags()).containsExactly("tag-a", "tag-b");
        assertThat(result.configuration()).isNotNull();
        assertThat(result.configuration().name()).isEqualTo("Config 1");
        assertThat(result.configuration().federation().id()).isEqualTo("fed-1");
    }
}
