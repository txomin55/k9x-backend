package com.k9x.application.events.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.dto.ConfigurationDTO;
import com.k9x.application.disciplines.use_case.dto.ConfigurationsDTO;
import com.k9x.domain.events.exceptions.EventAlreadyDeletedException;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.use_case.GetObdxClassificationServiceCase;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.snapshot.use_case.GetEventSnapshotServiceCase;
import com.k9x.application.events.use_case.dto.EventClassificationContextDTO;
import com.k9x.application.events.use_case.port.EventClassificationCacheManagerPort;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.stages.aggregates.StageSnapshot;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetEventClassificationServiceCaseTest {

    private static final EventSnapshot ACTIVE_EVENT = new EventSnapshot(
            "evt-1", "OBDX_RSCE_GRADE_1.V0", "obdx", "Open Grade 1", "stage-1", "creator@test.com",
            null, 1000L, 1000L, null, ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), List.of(), null);
    private static final EventSnapshot DELETED_EVENT = new EventSnapshot(
            "evt-1", "OBDX_RSCE_GRADE_1.V0", "obdx", "Open", "stage-1", "creator@test.com",
            null, 1000L, 1000L, 9999L, ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), List.of(), null);

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;
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
                getCompetitionPersistencePort,
                eventClassificationCacheManagerPort, getObdxClassificationServiceCase,
                getObdxFederationsConfigurationsPort, getEventSnapshotServiceCase);
    }

    private CompetitionSnapshot competition(EventSnapshot event) {
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage A", "comp-1", "user-1", 0L, Long.MAX_VALUE, 1000L, 1000L,
                null, List.of(event));
        return new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                0L, 0L, null, List.of(stage));
    }

    @Test
    void serves_persisted_snapshot_without_computing_when_present() {
        FetchClassificationDTO snapshot = new FetchClassificationDTO(
                "evt-1", "Open Grade 1", "FINISHED", "stage-1", "Stage A", "WC",
                "obdx", "OBDX_RSCE_GRADE_1.V0", "Grade 1", 5000L,
                new FetchObdxClassificationDTO(5000L, List.of(), "AVG", List.of()), "A+");
        // The context is resolved first (that is where the discipline comes from), then the snapshot is looked up
        // by (eventId, discipline); when it exists the aggregate is not recomputed.
        when(eventClassificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);
        when(getCompetitionPersistencePort.competitionIdByEvent("evt-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition(ACTIVE_EVENT));
        when(getEventSnapshotServiceCase.getSnapshot("evt-1", "obdx")).thenReturn(Optional.of(snapshot));

        FetchClassificationDTO result = serviceCase.getClassification("evt-1");

        assertThat(result).isSameAs(snapshot);
        verifyNoInteractions(getObdxClassificationServiceCase, getObdxFederationsConfigurationsPort);
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(eventClassificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);
        when(getCompetitionPersistencePort.competitionIdByEvent("evt-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.getClassification("evt-1"))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(getObdxClassificationServiceCase);
    }

    @Test
    void throws_exception_when_event_is_deleted() {
        when(eventClassificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);
        when(getCompetitionPersistencePort.competitionIdByEvent("evt-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition(DELETED_EVENT));

        assertThatThrownBy(() -> serviceCase.getClassification("evt-1"))
                .isInstanceOf(EventAlreadyDeletedException.class);

        verifyNoInteractions(getObdxClassificationServiceCase);
    }

    @Test
    void fetches_from_db_and_caches_context_on_cache_miss() throws IOException {
        FetchObdxClassificationDTO obdx = new FetchObdxClassificationDTO(5000L, List.of(), "AVG", List.of());
        when(eventClassificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);
        when(getCompetitionPersistencePort.competitionIdByEvent("evt-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition(ACTIVE_EVENT));
        when(getObdxClassificationServiceCase.getClassification(ACTIVE_EVENT)).thenReturn(obdx);
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenReturn(List.of(
                new ConfigurationsDTO(null, List.of(new ConfigurationDTO("OBDX_RSCE_GRADE_1.V0", "Grade 1", List.of())))));

        FetchClassificationDTO result = serviceCase.getClassification("evt-1");

        assertThat(result.eventId()).isEqualTo("evt-1");
        assertThat(result.stageName()).isEqualTo("Stage A");
        assertThat(result.competitionName()).isEqualTo("WC");
        assertThat(result.disciplineId()).isEqualTo("obdx");
        assertThat(result.configurationId()).isEqualTo("OBDX_RSCE_GRADE_1.V0");
        assertThat(result.configurationName()).isEqualTo("Grade 1");
        assertThat(result.obdx()).isSameAs(obdx);
        verify(eventClassificationCacheManagerPort)
                .put("evt-1", new EventClassificationContextDTO(ACTIVE_EVENT, "Stage A", Long.MAX_VALUE, "WC"));
    }

    @Test
    void uses_cached_context_without_hitting_db_on_cache_hit() throws IOException {
        FetchObdxClassificationDTO obdx = new FetchObdxClassificationDTO(5000L, List.of(), "AVG", List.of());
        when(eventClassificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt()))
                .thenReturn(new EventClassificationContextDTO(ACTIVE_EVENT, "Stage A", Long.MAX_VALUE, "WC"));
        when(getObdxClassificationServiceCase.getClassification(ACTIVE_EVENT)).thenReturn(obdx);
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenReturn(List.of());

        FetchClassificationDTO result = serviceCase.getClassification("evt-1");

        assertThat(result.stageName()).isEqualTo("Stage A");
        assertThat(result.competitionName()).isEqualTo("WC");
        assertThat(result.obdx()).isSameAs(obdx);
        verifyNoInteractions(getCompetitionPersistencePort);
        verify(eventClassificationCacheManagerPort, never()).put(any(), any());
    }
}
