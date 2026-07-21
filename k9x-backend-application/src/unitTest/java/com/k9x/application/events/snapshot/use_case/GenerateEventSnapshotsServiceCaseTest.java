package com.k9x.application.events.snapshot.use_case;

import com.k9x.application.events.obdx.use_case.dto.FetchClassificationCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.snapshot.port.GetPendingSnapshotEventsPersistencePort;
import com.k9x.application.events.snapshot.port.SaveObdxSnapshotPersistencePort;
import com.k9x.application.events.snapshot.port.payload.ObdxCompetitorPosition;
import com.k9x.application.events.snapshot.use_case.dto.PendingSnapshotEventDTO;
import com.k9x.application.events.use_case.GetEventClassificationServiceCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateEventSnapshotsServiceCaseTest {

    @Mock
    private GetPendingSnapshotEventsPersistencePort getPendingSnapshotEventsPersistencePort;
    @Mock
    private GetEventClassificationServiceCase getEventClassificationServiceCase;
    @Mock
    private SaveObdxSnapshotPersistencePort saveObdxSnapshotPersistencePort;

    private GenerateEventSnapshotsServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GenerateEventSnapshotsServiceCase(
                getPendingSnapshotEventsPersistencePort,
                getEventClassificationServiceCase,
                saveObdxSnapshotPersistencePort);
    }

    private FetchObdxClassificationDTO obdx(FetchClassificationCompetitorDTO... competitors) {
        return new FetchObdxClassificationDTO(null, List.of(competitors), "AVG", List.of());
    }

    private FetchClassificationDTO classification(String eventId, FetchObdxClassificationDTO obdx) {
        return new FetchClassificationDTO(eventId, "Event", "FINISHED", "stage-1", "Stage A", "WC",
                "obdx", "cfg", "Cfg", null, obdx, "A+");
    }

    private FetchClassificationCompetitorDTO competitor(String dogId, int position, BigDecimal rankScore) {
        return new FetchClassificationCompetitorDTO(dogId, dogId, "Border Collie", "o", "h", "t", "ES",
                (short) 5, (short) 7, position, null, null, false, "OK", false, false, false,
                List.of(), List.of(), null, rankScore);
    }

    private FetchClassificationCompetitorDTO competitorWithAwards(String dogId, int position, List<String> awards) {
        return new FetchClassificationCompetitorDTO(dogId, dogId, "Border Collie", "o", "h", "t", "ES",
                (short) 5, (short) 7, position, null, null, false, "OK", false, false, false,
                List.of(), awards, null, null);
    }

    @Test
    void does_nothing_when_there_are_no_pending_events() {
        when(getPendingSnapshotEventsPersistencePort.getFinishedEventsWithoutSnapshot(anyLong()))
                .thenReturn(List.of());

        serviceCase.generateSnapshots();

        verifyNoInteractions(getEventClassificationServiceCase, saveObdxSnapshotPersistencePort);
    }

    @Test
    void saves_only_the_obdx_payload_for_each_pending_event() {
        FetchObdxClassificationDTO o1 = obdx();
        FetchObdxClassificationDTO o2 = obdx();
        when(getPendingSnapshotEventsPersistencePort.getFinishedEventsWithoutSnapshot(anyLong()))
                .thenReturn(List.of(new PendingSnapshotEventDTO("evt-1", "obdx"),
                        new PendingSnapshotEventDTO("evt-2", "obdx")));
        when(getEventClassificationServiceCase.getClassification("evt-1")).thenReturn(classification("evt-1", o1));
        when(getEventClassificationServiceCase.getClassification("evt-2")).thenReturn(classification("evt-2", o2));

        serviceCase.generateSnapshots();

        verify(saveObdxSnapshotPersistencePort).save(eq("evt-1"), anyLong(), eq(o1), eq(List.of()), eq(List.of()));
        verify(saveObdxSnapshotPersistencePort).save(eq("evt-2"), anyLong(), eq(o2), eq(List.of()), eq(List.of()));
    }

    @Test
    void persists_the_deduplicated_union_of_granted_awards() {
        FetchObdxClassificationDTO obdx = obdx(
                competitorWithAwards("dog-1", 1, List.of("CACIOB")),
                competitorWithAwards("dog-2", 2, List.of("RCACIOB")),
                competitorWithAwards("dog-3", 3, List.of()));
        when(getPendingSnapshotEventsPersistencePort.getFinishedEventsWithoutSnapshot(anyLong()))
                .thenReturn(List.of(new PendingSnapshotEventDTO("evt-1", "obdx")));
        when(getEventClassificationServiceCase.getClassification("evt-1")).thenReturn(classification("evt-1", obdx));

        serviceCase.generateSnapshots();

        verify(saveObdxSnapshotPersistencePort).save(eq("evt-1"), anyLong(), eq(obdx), anyList(),
                eq(List.of("CACIOB", "RCACIOB")));
    }

    @Test
    void persists_the_tie_aware_positions_and_rank_scores_with_the_snapshot() {
        FetchObdxClassificationDTO obdx = obdx(
                competitor("dog-1", 1, new BigDecimal("475.50")),
                competitor("dog-2", 1, new BigDecimal("475.50")),
                competitor("dog-3", 3, new BigDecimal("410.00")));
        when(getPendingSnapshotEventsPersistencePort.getFinishedEventsWithoutSnapshot(anyLong()))
                .thenReturn(List.of(new PendingSnapshotEventDTO("evt-1", "obdx")));
        when(getEventClassificationServiceCase.getClassification("evt-1")).thenReturn(classification("evt-1", obdx));

        serviceCase.generateSnapshots();

        List<ObdxCompetitorPosition> expected = List.of(
                new ObdxCompetitorPosition("dog-1", (short) 1, new BigDecimal("475.50")),
                new ObdxCompetitorPosition("dog-2", (short) 1, new BigDecimal("475.50")),
                new ObdxCompetitorPosition("dog-3", (short) 3, new BigDecimal("410.00")));
        verify(saveObdxSnapshotPersistencePort).save(eq("evt-1"), anyLong(), eq(obdx), eq(expected), eq(List.of()));
    }

    @Test
    void persists_empty_competitors_when_classification_has_no_obdx_payload() {
        when(getPendingSnapshotEventsPersistencePort.getFinishedEventsWithoutSnapshot(anyLong()))
                .thenReturn(List.of(new PendingSnapshotEventDTO("evt-1", "obdx")));
        when(getEventClassificationServiceCase.getClassification("evt-1")).thenReturn(classification("evt-1", null));

        serviceCase.generateSnapshots();

        verify(saveObdxSnapshotPersistencePort).save(eq("evt-1"), anyLong(), isNull(), eq(List.of()), eq(List.of()));
    }

    @Test
    void skips_events_of_unsupported_discipline() {
        FetchObdxClassificationDTO o2 = obdx();
        when(getPendingSnapshotEventsPersistencePort.getFinishedEventsWithoutSnapshot(anyLong()))
                .thenReturn(List.of(new PendingSnapshotEventDTO("evt-1", "rally"),
                        new PendingSnapshotEventDTO("evt-2", "obdx")));
        when(getEventClassificationServiceCase.getClassification("evt-2")).thenReturn(classification("evt-2", o2));

        serviceCase.generateSnapshots();

        verify(getEventClassificationServiceCase, never()).getClassification("evt-1");
        verify(saveObdxSnapshotPersistencePort, never()).save(eq("evt-1"), anyLong(), any(), any(), any());
        verify(saveObdxSnapshotPersistencePort).save(eq("evt-2"), anyLong(), eq(o2), eq(List.of()), eq(List.of()));
    }

    @Test
    void one_failing_event_does_not_abort_the_rest() {
        FetchObdxClassificationDTO o2 = obdx();
        when(getPendingSnapshotEventsPersistencePort.getFinishedEventsWithoutSnapshot(anyLong()))
                .thenReturn(List.of(new PendingSnapshotEventDTO("evt-1", "obdx"),
                        new PendingSnapshotEventDTO("evt-2", "obdx")));
        when(getEventClassificationServiceCase.getClassification("evt-1"))
                .thenThrow(new RuntimeException("boom"));
        when(getEventClassificationServiceCase.getClassification("evt-2")).thenReturn(classification("evt-2", o2));

        serviceCase.generateSnapshots();

        verify(saveObdxSnapshotPersistencePort, never()).save(eq("evt-1"), anyLong(), any(), any(), any());
        verify(saveObdxSnapshotPersistencePort).save(eq("evt-2"), anyLong(), eq(o2), eq(List.of()), eq(List.of()));
    }
}
