package com.k9x.application.events.snapshot.use_case;

import com.k9x.application.events.obdx.use_case.dto.FetchClassificationCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.snapshot.port.GetPendingSnapshotEventsPersistencePort;
import com.k9x.application.events.snapshot.port.SaveObdxEventSnapshotPersistencePort;
import com.k9x.application.events.snapshot.port.UpdateObdxCompetitorPositionsPersistencePort;
import com.k9x.application.events.snapshot.port.payload.ObdxCompetitorPosition;
import com.k9x.application.events.snapshot.use_case.dto.PendingSnapshotEventDTO;
import com.k9x.application.events.use_case.GetEventClassificationServiceCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
    @Mock
    private UpdateObdxCompetitorPositionsPersistencePort updateObdxCompetitorPositionsPersistencePort;

    private GenerateEventSnapshotsServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GenerateEventSnapshotsServiceCase(
                getPendingSnapshotEventsPersistencePort,
                getEventClassificationServiceCase,
                saveObdxEventSnapshotPersistencePort,
                updateObdxCompetitorPositionsPersistencePort);
    }

    private FetchClassificationDTO classification(String eventId) {
        return classification(eventId, null);
    }

    private FetchClassificationDTO classification(String eventId, FetchObdxClassificationDTO obdx) {
        return new FetchClassificationDTO(eventId, "Event", "FINISHED", "stage-1", "Stage A", "WC",
                "obdx", "cfg", "Cfg", null, obdx, "A+");
    }

    private FetchClassificationCompetitorDTO competitor(String dogId, int position) {
        return new FetchClassificationCompetitorDTO(dogId, dogId, "Border Collie", "o", "h", "t", "ES",
                (short) 5, (short) 7, position, null, null, false, "OK", false, false, false,
                List.of(), List.of(), null);
    }

    @Test
    void does_nothing_when_there_are_no_pending_events() {
        when(getPendingSnapshotEventsPersistencePort.getFinishedEventsWithoutSnapshot(anyLong()))
                .thenReturn(List.of());

        serviceCase.generateSnapshots();

        verifyNoInteractions(getEventClassificationServiceCase, saveObdxEventSnapshotPersistencePort,
                updateObdxCompetitorPositionsPersistencePort);
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
    void persists_the_tie_aware_positions_before_saving_the_snapshot() {
        FetchObdxClassificationDTO obdx = new FetchObdxClassificationDTO(null,
                List.of(competitor("dog-1", 1), competitor("dog-2", 1), competitor("dog-3", 3)),
                "MID_AVG", List.of());
        FetchClassificationDTO classification = classification("evt-1", obdx);
        when(getPendingSnapshotEventsPersistencePort.getFinishedEventsWithoutSnapshot(anyLong()))
                .thenReturn(List.of(new PendingSnapshotEventDTO("evt-1", "obdx")));
        when(getEventClassificationServiceCase.getClassification("evt-1")).thenReturn(classification);

        serviceCase.generateSnapshots();

        List<ObdxCompetitorPosition> expected = List.of(
                new ObdxCompetitorPosition("dog-1", (short) 1),
                new ObdxCompetitorPosition("dog-2", (short) 1),
                new ObdxCompetitorPosition("dog-3", (short) 3));
        // Positions must be written first; the snapshot save is the "done" marker.
        InOrder inOrder = inOrder(updateObdxCompetitorPositionsPersistencePort, saveObdxEventSnapshotPersistencePort);
        inOrder.verify(updateObdxCompetitorPositionsPersistencePort).updatePositions("evt-1", expected);
        inOrder.verify(saveObdxEventSnapshotPersistencePort).save(eq("evt-1"), anyLong(), eq(classification));
    }

    @Test
    void persists_empty_positions_when_classification_has_no_obdx_payload() {
        FetchClassificationDTO classification = classification("evt-1");
        when(getPendingSnapshotEventsPersistencePort.getFinishedEventsWithoutSnapshot(anyLong()))
                .thenReturn(List.of(new PendingSnapshotEventDTO("evt-1", "obdx")));
        when(getEventClassificationServiceCase.getClassification("evt-1")).thenReturn(classification);

        serviceCase.generateSnapshots();

        verify(updateObdxCompetitorPositionsPersistencePort).updatePositions("evt-1", List.of());
        verify(saveObdxEventSnapshotPersistencePort).save(eq("evt-1"), anyLong(), eq(classification));
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
