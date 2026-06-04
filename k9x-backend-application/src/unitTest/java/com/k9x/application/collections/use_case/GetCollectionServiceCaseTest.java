package com.k9x.application.collections.use_case;

import com.k9x.application.collections.obdx.port.GetObdxCollectionEventJudgesPersistencePort;
import com.k9x.application.collections.obdx.use_case.GetObdxCollectionServiceCase;
import com.k9x.application.collections.obdx.use_case.dto.FetchObdxCollectionDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionDetailDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionJudgeWithCollectorDTO;
import com.k9x.application.disciplines.obdx.port.GetObdxConfigurationAllowedValuesPort;
import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.exceptions.ObdxUserNotCollectorException;
import com.k9x.application.events.obdx.use_cases.port.GetEventPersistencePort;
import com.k9x.application.stages.exceptions.StageExpiredException;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.stages.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCollectionServiceCaseTest {

    private static final Event ACTIVE_EVENT = new Event("event-1", "config-1", "obdx", "Event A", "stage-1", "creator@test.com", 0L, 0L, null, ObdxAvgMethod.MID_AVG);
    private static final Stage ACTIVE_STAGE = new Stage("stage-1", "Stage A", "comp-1", "user-1", Long.MAX_VALUE, 0L, 0L, null);
    private static final FetchObdxCollectionDTO OBDX = new FetchObdxCollectionDTO(List.of());
    @Mock
    private GetEventPersistencePort getEventPersistencePort;
    @Mock
    private GetStagePersistencePort getStagePersistencePort;
    @Mock
    private GetObdxCollectionEventJudgesPersistencePort getObdxCollectionEventJudgesPersistencePort;
    @Mock
    private GetObdxConfigurationAllowedValuesPort getObdxConfigurationAllowedValuesPort;
    @Mock
    private GetObdxCollectionServiceCase getObdxCollectionServiceCase;
    @Captor
    private ArgumentCaptor<List<FetchCollectionJudgeWithCollectorDTO>> visibleJudgesCaptor;
    private GetCollectionServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetCollectionServiceCase(
                getEventPersistencePort,
                getStagePersistencePort,
                getObdxCollectionEventJudgesPersistencePort,
                getObdxConfigurationAllowedValuesPort,
                getObdxCollectionServiceCase);
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getEventPersistencePort.getEvent("event-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.getCollection("event-1", "user@test.com"))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(getStagePersistencePort, getObdxCollectionEventJudgesPersistencePort,
                getObdxConfigurationAllowedValuesPort, getObdxCollectionServiceCase);
    }

    @Test
    void throws_exception_when_event_is_deleted() {
        Event deletedEvent = new Event("event-1", "config-1", "obdx", "Event A", "stage-1", "creator@test.com", 0L, 0L, 1700000000000L, ObdxAvgMethod.MID_AVG);
        when(getEventPersistencePort.getEvent("event-1")).thenReturn(deletedEvent);

        assertThatThrownBy(() -> serviceCase.getCollection("event-1", "user@test.com"))
                .isInstanceOf(EventAlreadyDeletedException.class);

        verifyNoInteractions(getStagePersistencePort, getObdxCollectionEventJudgesPersistencePort,
                getObdxConfigurationAllowedValuesPort, getObdxCollectionServiceCase);
    }

    @Test
    void throws_exception_when_stage_is_expired() {
        Stage expiredStage = new Stage("stage-1", "Stage A", "comp-1", "user-1", 1L, 0L, 0L, null);
        when(getEventPersistencePort.getEvent("event-1")).thenReturn(ACTIVE_EVENT);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(expiredStage);

        assertThatThrownBy(() -> serviceCase.getCollection("event-1", "user@test.com"))
                .isInstanceOf(StageExpiredException.class);

        verifyNoInteractions(getObdxCollectionEventJudgesPersistencePort,
                getObdxConfigurationAllowedValuesPort, getObdxCollectionServiceCase);
    }

    @Test
    void throws_exception_when_user_is_not_collector_and_not_creator() {
        List<FetchCollectionJudgeWithCollectorDTO> judges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "other@test.com")
        );
        when(getEventPersistencePort.getEvent("event-1")).thenReturn(ACTIVE_EVENT);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(ACTIVE_STAGE);
        when(getObdxCollectionEventJudgesPersistencePort.getJudges("event-1")).thenReturn(judges);

        assertThatThrownBy(() -> serviceCase.getCollection("event-1", "notcollector@test.com"))
                .isInstanceOf(ObdxUserNotCollectorException.class);

        verifyNoInteractions(getObdxCollectionServiceCase);
    }

    @Test
    void delegates_all_judges_and_builds_detail_when_user_is_creator() {
        List<FetchCollectionJudgeWithCollectorDTO> judges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "collector1@test.com"),
                new FetchCollectionJudgeWithCollectorDTO("judge-2", "Judge Two", "collector2@test.com")
        );
        when(getEventPersistencePort.getEvent("event-1")).thenReturn(ACTIVE_EVENT);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(ACTIVE_STAGE);
        when(getObdxCollectionEventJudgesPersistencePort.getJudges("event-1")).thenReturn(judges);
        when(getObdxConfigurationAllowedValuesPort.getAllowedValues("config-1")).thenReturn(List.of(new BigDecimal("7.5")));
        when(getObdxCollectionServiceCase.getCollection(eq("event-1"), any())).thenReturn(OBDX);

        FetchCollectionDetailDTO result = serviceCase.getCollection("event-1", "creator@test.com");

        assertThat(result.configurationId()).isEqualTo("config-1");
        assertThat(result.discipline()).isEqualTo("obdx");
        assertThat(result.allowedValues()).containsExactly(new BigDecimal("7.5"));
        assertThat(result.obdx()).isSameAs(OBDX);

        verify(getObdxCollectionServiceCase).getCollection(eq("event-1"), visibleJudgesCaptor.capture());
        assertThat(visibleJudgesCaptor.getValue()).containsExactlyElementsOf(judges);
    }

    @Test
    void delegates_only_collector_judges_when_user_is_collector() {
        List<FetchCollectionJudgeWithCollectorDTO> judges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "collector@test.com"),
                new FetchCollectionJudgeWithCollectorDTO("judge-2", "Judge Two", "other@test.com")
        );
        when(getEventPersistencePort.getEvent("event-1")).thenReturn(ACTIVE_EVENT);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(ACTIVE_STAGE);
        when(getObdxCollectionEventJudgesPersistencePort.getJudges("event-1")).thenReturn(judges);
        when(getObdxCollectionServiceCase.getCollection(eq("event-1"), any())).thenReturn(OBDX);

        serviceCase.getCollection("event-1", "collector@test.com");

        verify(getObdxCollectionServiceCase).getCollection(eq("event-1"), visibleJudgesCaptor.capture());
        assertThat(visibleJudgesCaptor.getValue())
                .extracting(FetchCollectionJudgeWithCollectorDTO::judgeId)
                .containsExactly("judge-1");
    }
}
