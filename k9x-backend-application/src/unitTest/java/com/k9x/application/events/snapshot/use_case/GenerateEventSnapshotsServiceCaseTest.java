package com.k9x.application.events.snapshot.use_case;

import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.snapshot.port.GetPendingSnapshotEventsPersistencePort;
import com.k9x.application.events.snapshot.port.SaveObdxEventSnapshotPersistencePort;
import com.k9x.application.events.snapshot.use_case.dto.PendingSnapshotEventDTO;
import com.k9x.application.events.use_case.GetEventClassificationServiceCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateEventSnapshotsServiceCaseTest {

    @Mock
    private GetPendingSnapshotEventsPersistencePort getPendingSnapshotEventsPersistencePort;
    @Mock
    private GetEventClassificationServiceCase getEventClassificationServiceCase;
    @Mock
    private SaveObdxEventSnapshotPersistencePort saveObdxEventSnapshotPersistencePort;

    private GenerateEventSnapshotsServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GenerateEventSnapshotsServiceCase(
                getPendingSnapshotEventsPersistencePort,
                getEventClassificationServiceCase,
                saveObdxEventSnapshotPersistencePort);
    }

    private FetchClassificationDTO classification(String eventId) {
        return new FetchClassificationDTO(eventId, "Event", "FINISHED", "stage-1", "Stage A", "WC",
                "obdx", "cfg", "Cfg", null, null);
    }

    @Test
    void does_nothing_when_there_are_no_pending_events() {
        when(getPendingSnapshotEventsPersistencePort.getFinishedEventsWithoutSnapshot(anyLong()))
                .thenReturn(List.of());

        serviceCase.generateSnapshots();

        verifyNoInteractions(getEventClassificationServiceCase, saveObdxEventSnapshotPersistencePort);
    }

    @Test
    void computes_and_saves_a_snapshot_for_each_pending_obdx_event() {
        FetchClassificationDTO c1 = classification("evt-1");
        FetchClassificationDTO c2 = classification("evt-2");
        when(getPendingSnapshotEventsPersistencePort.getFinishedEventsWithoutSnapshot(anyLong()))
                .thenReturn(List.of(new PendingSnapshotEventDTO("evt-1", "obdx"),
                        new PendingSnapshotEventDTO("evt-2", "obdx")));
        when(getEventClassificationServiceCase.getClassification("evt-1")).thenReturn(c1);
        when(getEventClassificationServiceCase.getClassification("evt-2")).thenReturn(c2);

        serviceCase.generateSnapshots();

        verify(saveObdxEventSnapshotPersistencePort).save(eq("evt-1"), anyLong(), eq(c1));
        verify(saveObdxEventSnapshotPersistencePort).save(eq("evt-2"), anyLong(), eq(c2));
    }

    @Test
    void skips_events_of_unsupported_discipline() {
        FetchClassificationDTO obdx = classification("evt-2");
        when(getPendingSnapshotEventsPersistencePort.getFinishedEventsWithoutSnapshot(anyLong()))
                .thenReturn(List.of(new PendingSnapshotEventDTO("evt-1", "rally"),
                        new PendingSnapshotEventDTO("evt-2", "obdx")));
        when(getEventClassificationServiceCase.getClassification("evt-2")).thenReturn(obdx);

        serviceCase.generateSnapshots();

        // The unknown discipline is skipped (never computed nor saved); the OBDX one is still processed.
        verify(getEventClassificationServiceCase, never()).getClassification("evt-1");
        verify(saveObdxEventSnapshotPersistencePort, never()).save(eq("evt-1"), anyLong(), any());
        verify(saveObdxEventSnapshotPersistencePort).save(eq("evt-2"), anyLong(), eq(obdx));
    }

    @Test
    void one_failing_event_does_not_abort_the_rest() {
        FetchClassificationDTO c2 = classification("evt-2");
        when(getPendingSnapshotEventsPersistencePort.getFinishedEventsWithoutSnapshot(anyLong()))
                .thenReturn(List.of(new PendingSnapshotEventDTO("evt-1", "obdx"),
                        new PendingSnapshotEventDTO("evt-2", "obdx")));
        when(getEventClassificationServiceCase.getClassification("evt-1"))
                .thenThrow(new RuntimeException("boom"));
        when(getEventClassificationServiceCase.getClassification("evt-2")).thenReturn(c2);

        serviceCase.generateSnapshots();

        verify(saveObdxEventSnapshotPersistencePort, never()).save(eq("evt-1"), anyLong(), any());
        verify(saveObdxEventSnapshotPersistencePort).save(eq("evt-2"), anyLong(), eq(c2));
    }
}
