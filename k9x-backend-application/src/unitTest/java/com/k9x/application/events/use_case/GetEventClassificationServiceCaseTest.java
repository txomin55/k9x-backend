package com.k9x.application.events.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.use_case.GetObdxClassificationServiceCase;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.use_case.dto.EventClassificationContextDTO;
import com.k9x.application.events.use_case.port.EventClassificationCacheManagerPort;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.stages.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetEventClassificationServiceCaseTest {

    private static final Event ACTIVE_EVENT = new Event(
            "evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1", "stage-1", "creator@test.com",
            1000L, 1000L, null, ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of());
    private static final Event DELETED_EVENT = new Event(
            "evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open", "stage-1", "creator@test.com",
            1000L, 1000L, 9999L, ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of());

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;
    @Mock
    private EventClassificationCacheManagerPort eventClassificationCacheManagerPort;
    @Mock
    private GetObdxClassificationServiceCase getObdxClassificationServiceCase;

    private GetEventClassificationServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetEventClassificationServiceCase(
                getCompetitionPersistencePort,
                eventClassificationCacheManagerPort, getObdxClassificationServiceCase);
    }

    private Competition competition(Event event) {
        Stage stage = new Stage("stage-1", "Stage A", "comp-1", "user-1", 0L, Long.MAX_VALUE, 1000L, 1000L,
                null, List.of(event));
        return new Competition("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                0L, 0L, null, List.of(stage));
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
    void fetches_from_db_and_caches_context_on_cache_miss() {
        FetchObdxClassificationDTO obdx = new FetchObdxClassificationDTO(5000L, List.of());
        when(eventClassificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);
        when(getCompetitionPersistencePort.competitionIdByEvent("evt-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition(ACTIVE_EVENT));
        when(getObdxClassificationServiceCase.getClassification(ACTIVE_EVENT)).thenReturn(obdx);

        FetchClassificationDTO result = serviceCase.getClassification("evt-1");

        assertThat(result.eventId()).isEqualTo("evt-1");
        assertThat(result.stageName()).isEqualTo("Stage A");
        assertThat(result.obdx()).isSameAs(obdx);
        verify(eventClassificationCacheManagerPort)
                .put("evt-1", new EventClassificationContextDTO(ACTIVE_EVENT, "Stage A"));
    }

    @Test
    void uses_cached_context_without_hitting_db_on_cache_hit() {
        FetchObdxClassificationDTO obdx = new FetchObdxClassificationDTO(5000L, List.of());
        when(eventClassificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt()))
                .thenReturn(new EventClassificationContextDTO(ACTIVE_EVENT, "Stage A"));
        when(getObdxClassificationServiceCase.getClassification(ACTIVE_EVENT)).thenReturn(obdx);

        FetchClassificationDTO result = serviceCase.getClassification("evt-1");

        assertThat(result.stageName()).isEqualTo("Stage A");
        assertThat(result.obdx()).isSameAs(obdx);
        verifyNoInteractions(getCompetitionPersistencePort);
        verify(eventClassificationCacheManagerPort, never()).put(any(), any());
    }
}
