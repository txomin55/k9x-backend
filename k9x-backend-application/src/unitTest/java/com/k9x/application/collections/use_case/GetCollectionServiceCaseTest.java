package com.k9x.application.collections.use_case;

import com.k9x.application.collections.obdx.port.GetObdxCollectionEventJudgesPersistencePort;
import com.k9x.application.collections.obdx.use_case.GetObdxCollectionServiceCase;
import com.k9x.application.collections.obdx.use_case.dto.FetchObdxCollectionDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionDetailDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionJudgeWithCollectorDTO;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.disciplines.obdx.port.GetObdxConfigurationAllowedValuesPort;
import com.k9x.domain.events.exceptions.EventAlreadyDeletedException;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.exceptions.ObdxUserNotCollectorException;
import com.k9x.domain.stages.exceptions.StageExpiredException;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.stages.aggregates.StageSnapshot;
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

    private static final FetchObdxCollectionDTO OBDX = new FetchObdxCollectionDTO(List.of());
    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;
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
                getCompetitionPersistencePort,
                getObdxCollectionEventJudgesPersistencePort,
                getObdxConfigurationAllowedValuesPort,
                getObdxCollectionServiceCase);
    }

    private EventSnapshot event(Long deletedAt) {
        return new EventSnapshot("event-1", "config-1", "obdx", "Event A", "stage-1", "creator@test.com", null, 0L, 0L, deletedAt,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), List.of(), null, null, null, null);
    }

    private CompetitionSnapshot competition(EventSnapshot event, long dateTo) {
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage A", "comp-1", "user-1", 0L, dateTo, 0L, 0L, null, List.of(event));
        return new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                0L, 0L, null, List.of(stage));
    }

    private void givenActiveEventAndStage() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(event(null), Long.MAX_VALUE));
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.getCollection("event-1", "user@test.com"))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(getObdxCollectionEventJudgesPersistencePort,
                getObdxConfigurationAllowedValuesPort, getObdxCollectionServiceCase);
    }

    @Test
    void throws_exception_when_event_is_deleted() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(event(1700000000000L), Long.MAX_VALUE));

        assertThatThrownBy(() -> serviceCase.getCollection("event-1", "user@test.com"))
                .isInstanceOf(EventAlreadyDeletedException.class);

        verifyNoInteractions(getObdxCollectionEventJudgesPersistencePort,
                getObdxConfigurationAllowedValuesPort, getObdxCollectionServiceCase);
    }

    @Test
    void throws_exception_when_stage_is_expired() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(event(null), 1L));

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
        givenActiveEventAndStage();
        when(getObdxCollectionEventJudgesPersistencePort.getJudges("event-1")).thenReturn(judges);

        assertThatThrownBy(() -> serviceCase.getCollection("event-1", "notcollector@test.com"))
                .isInstanceOf(ObdxUserNotCollectorException.class);

        verifyNoInteractions(getObdxCollectionServiceCase);
    }

    @Test
    void allows_creator_to_view_expired_stage() {
        List<FetchCollectionJudgeWithCollectorDTO> judges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "collector1@test.com")
        );
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(event(null), 1L));
        when(getObdxCollectionEventJudgesPersistencePort.getJudges("event-1")).thenReturn(judges);
        when(getObdxConfigurationAllowedValuesPort.getAllowedValues("config-1")).thenReturn(List.of(new BigDecimal("7.5")));
        when(getObdxCollectionServiceCase.getCollection(eq("event-1"), any())).thenReturn(OBDX);

        FetchCollectionDetailDTO result = serviceCase.getCollection("event-1", "creator@test.com");

        assertThat(result.obdx()).isSameAs(OBDX);
        verify(getObdxCollectionServiceCase).getCollection(eq("event-1"), visibleJudgesCaptor.capture());
        assertThat(visibleJudgesCaptor.getValue()).containsExactlyElementsOf(judges);
    }

    @Test
    void delegates_all_judges_and_builds_detail_when_user_is_creator() {
        List<FetchCollectionJudgeWithCollectorDTO> judges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "collector1@test.com"),
                new FetchCollectionJudgeWithCollectorDTO("judge-2", "Judge Two", "collector2@test.com")
        );
        givenActiveEventAndStage();
        when(getObdxCollectionEventJudgesPersistencePort.getJudges("event-1")).thenReturn(judges);
        when(getObdxConfigurationAllowedValuesPort.getAllowedValues("config-1")).thenReturn(List.of(new BigDecimal("7.5")));
        when(getObdxCollectionServiceCase.getCollection(eq("event-1"), any())).thenReturn(OBDX);

        FetchCollectionDetailDTO result = serviceCase.getCollection("event-1", "creator@test.com");

        assertThat(result.competitionName()).isEqualTo("WC");
        assertThat(result.eventName()).isEqualTo("Event A");
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
        givenActiveEventAndStage();
        when(getObdxCollectionEventJudgesPersistencePort.getJudges("event-1")).thenReturn(judges);
        when(getObdxCollectionServiceCase.getCollection(eq("event-1"), any())).thenReturn(OBDX);

        serviceCase.getCollection("event-1", "collector@test.com");

        verify(getObdxCollectionServiceCase).getCollection(eq("event-1"), visibleJudgesCaptor.capture());
        assertThat(visibleJudgesCaptor.getValue())
                .extracting(FetchCollectionJudgeWithCollectorDTO::judgeId)
                .containsExactly("judge-1");
    }
}
