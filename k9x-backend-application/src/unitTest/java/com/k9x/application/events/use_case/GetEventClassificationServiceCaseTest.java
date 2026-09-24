package com.k9x.application.events.use_case;

import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.dto.ConfigurationDTO;
import com.k9x.application.disciplines.use_case.dto.ConfigurationsDTO;
import com.k9x.application.events.obdx.use_case.GetObdxClassificationServiceCase;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.port.GetClassificationEventPersistencePort;
import com.k9x.application.events.port.GetEventClassificationHeaderPersistencePort;
import com.k9x.application.events.snapshot.use_case.GetEventSnapshotServiceCase;
import com.k9x.application.events.use_case.dto.EventClassificationContextDTO;
import com.k9x.application.events.use_case.dto.FetchEventClassificationHeaderDTO;
import com.k9x.application.events.use_case.port.EventClassificationCacheManagerPort;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.exceptions.EventAlreadyDeletedException;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetEventClassificationServiceCaseTest {

    private static final long FAR_PAST = 1_000L;

    private static final EventSnapshot ACTIVE_EVENT = new EventSnapshot(
            "evt-1", "OBDX.RSCE_GRADO_1.V2026", "obdx", "Open Grade 1", "stage-1", "creator@test.com",
            null, 1000L, 1000L, null, ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), List.of(), null, null, null);

    @Mock
    private GetEventClassificationHeaderPersistencePort getEventClassificationHeaderPersistencePort;
    @Mock
    private GetClassificationEventPersistencePort getClassificationEventPersistencePort;
    @Mock
    private EventClassificationCacheManagerPort eventClassificationCacheManagerPort;
    @Mock
    private GetObdxClassificationServiceCase getObdxClassificationServiceCase;
    @Mock
    private GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort;
    @Mock
    private GetEventSnapshotServiceCase getEventSnapshotServiceCase;

    private GetEventClassificationServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetEventClassificationServiceCase(
                getEventClassificationHeaderPersistencePort, getClassificationEventPersistencePort,
                eventClassificationCacheManagerPort, getObdxClassificationServiceCase,
                getObdxFederationsConfigurationsPort, getEventSnapshotServiceCase);
    }

    private static FetchEventClassificationHeaderDTO header(Long deletedAt, long stageDateTo) {
        return new FetchEventClassificationHeaderDTO("evt-1", "Open Grade 1", "obdx", "OBDX.RSCE_GRADO_1.V2026",
                deletedAt, null, true, "stage-1", "Stage A", stageDateTo, "WC", null);
    }

    private void cacheMiss() {
        when(eventClassificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);
    }

    @Test
    void serves_the_persisted_obdx_payload_of_a_finished_event_without_loading_a_score() throws IOException {
        FetchObdxClassificationDTO storedObdx = new FetchObdxClassificationDTO(5000L, List.of(), "AVG", List.of());
        cacheMiss();
        when(getEventClassificationHeaderPersistencePort.getHeader("evt-1")).thenReturn(Optional.of(header(null, FAR_PAST)));
        when(getEventSnapshotServiceCase.getSnapshot("evt-1", "obdx")).thenReturn(Optional.of(storedObdx));
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenReturn(List.of());

        FetchClassificationDTO result = serviceCase.getClassification("evt-1");

        assertThat(result.obdx()).isSameAs(storedObdx);
        assertThat(result.eventId()).isEqualTo("evt-1");
        assertThat(result.competitionName()).isEqualTo("WC");
        assertThat(result.eventStatus()).isEqualTo("FINISHED");
        verifyNoInteractions(getObdxClassificationServiceCase, getClassificationEventPersistencePort);
    }

    @Test
    void throws_exception_when_event_not_found() {
        cacheMiss();
        when(getEventClassificationHeaderPersistencePort.getHeader("evt-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceCase.getClassification("evt-1"))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(getObdxClassificationServiceCase);
    }

    @Test
    void throws_exception_when_event_is_deleted() {
        cacheMiss();
        when(getEventClassificationHeaderPersistencePort.getHeader("evt-1")).thenReturn(Optional.of(header(9999L, FAR_PAST)));

        assertThatThrownBy(() -> serviceCase.getClassification("evt-1"))
                .isInstanceOf(EventAlreadyDeletedException.class);

        verifyNoInteractions(getObdxClassificationServiceCase);
    }

    @Test
    void loads_only_the_event_to_compute_a_live_classification_and_caches_it() throws IOException {
        FetchObdxClassificationDTO obdx = new FetchObdxClassificationDTO(5000L, List.of(), "AVG", List.of());
        FetchEventClassificationHeaderDTO live = header(null, Long.MAX_VALUE);
        cacheMiss();
        when(getEventClassificationHeaderPersistencePort.getHeader("evt-1")).thenReturn(Optional.of(live));
        when(getEventSnapshotServiceCase.getSnapshot("evt-1", "obdx")).thenReturn(Optional.empty());
        when(getClassificationEventPersistencePort.getEvent("evt-1")).thenReturn(Optional.of(ACTIVE_EVENT));
        when(getObdxClassificationServiceCase.getClassification(ACTIVE_EVENT)).thenReturn(obdx);
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenReturn(List.of(
                new ConfigurationsDTO(null, List.of(new ConfigurationDTO("OBDX.RSCE_GRADO_1.V2026", "Grade 1", List.of())))));

        FetchClassificationDTO result = serviceCase.getClassification("evt-1");

        assertThat(result.stageName()).isEqualTo("Stage A");
        assertThat(result.competitionName()).isEqualTo("WC");
        assertThat(result.disciplineId()).isEqualTo("obdx");
        assertThat(result.configurationName()).isEqualTo("Grade 1");
        assertThat(result.obdx()).isSameAs(obdx);
        // Scored but no competitor settled (it has none) and the stage is still on: STARTED.
        assertThat(result.eventStatus()).isEqualTo("STARTED");
        verify(eventClassificationCacheManagerPort).put("evt-1", new EventClassificationContextDTO(live, null));
        verify(eventClassificationCacheManagerPort).put("evt-1", new EventClassificationContextDTO(live, ACTIVE_EVENT));
    }

    @Test
    void uses_the_cached_context_without_reading_again_on_cache_hit() throws IOException {
        FetchObdxClassificationDTO obdx = new FetchObdxClassificationDTO(5000L, List.of(), "AVG", List.of());
        when(eventClassificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt()))
                .thenReturn(new EventClassificationContextDTO(header(null, Long.MAX_VALUE), ACTIVE_EVENT));
        when(getEventSnapshotServiceCase.getSnapshot("evt-1", "obdx")).thenReturn(Optional.empty());
        when(getObdxClassificationServiceCase.getClassification(ACTIVE_EVENT)).thenReturn(obdx);
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenReturn(List.of());

        FetchClassificationDTO result = serviceCase.getClassification("evt-1");

        assertThat(result.stageName()).isEqualTo("Stage A");
        assertThat(result.obdx()).isSameAs(obdx);
        verifyNoInteractions(getEventClassificationHeaderPersistencePort, getClassificationEventPersistencePort);
        verify(eventClassificationCacheManagerPort, never()).put(any(), any());
    }
}
