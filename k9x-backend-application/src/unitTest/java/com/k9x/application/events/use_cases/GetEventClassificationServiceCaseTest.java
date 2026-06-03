package com.k9x.application.events.use_cases;

import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.use_cases.GetObdxClassificationServiceCase;
import com.k9x.application.events.obdx.use_cases.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_cases.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.obdx.use_cases.port.GetEventPersistencePort;
import com.k9x.application.events.use_cases.dto.EventClassificationContextDTO;
import com.k9x.application.events.use_cases.port.EventClassificationCacheManagerPort;
import com.k9x.application.stages.port.GetStagePersistencePort;
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
            1000L, 1000L, null, ObdxAvgMethod.MID_AVG);
    private static final Stage STAGE = new Stage(
            "stage-1", "Stage A", "comp-1", "user-1", Long.MAX_VALUE, 1000L, 1000L, null);

    @Mock
    private GetEventPersistencePort getEventPersistencePort;
    @Mock
    private GetStagePersistencePort getStagePersistencePort;
    @Mock
    private EventClassificationCacheManagerPort eventClassificationCacheManagerPort;
    @Mock
    private GetObdxClassificationServiceCase getObdxClassificationServiceCase;

    private GetEventClassificationServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetEventClassificationServiceCase(
                getEventPersistencePort, getStagePersistencePort,
                eventClassificationCacheManagerPort, getObdxClassificationServiceCase);
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(eventClassificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);
        when(getEventPersistencePort.getEvent("evt-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.getClassification("evt-1"))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(getObdxClassificationServiceCase);
    }

    @Test
    void throws_exception_when_event_is_deleted() {
        Event deleted = new Event("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open", "stage-1",
                "creator@test.com", 1000L, 1000L, 9999L, ObdxAvgMethod.MID_AVG);
        when(eventClassificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);
        when(getEventPersistencePort.getEvent("evt-1")).thenReturn(deleted);

        assertThatThrownBy(() -> serviceCase.getClassification("evt-1"))
                .isInstanceOf(EventAlreadyDeletedException.class);

        verifyNoInteractions(getObdxClassificationServiceCase);
    }

    @Test
    void fetches_from_db_and_caches_context_on_cache_miss() {
        FetchObdxClassificationDTO obdx = new FetchObdxClassificationDTO(5000L, List.of());
        when(eventClassificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);
        when(getEventPersistencePort.getEvent("evt-1")).thenReturn(ACTIVE_EVENT);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(STAGE);
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
        verifyNoInteractions(getEventPersistencePort, getStagePersistencePort);
        verify(eventClassificationCacheManagerPort, never()).put(any(), any());
    }
}
