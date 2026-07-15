package com.k9x.application.events.snapshot.use_case;

import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.snapshot.port.GetObdxEventSnapshotPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetEventSnapshotServiceCaseTest {

    @Mock
    private GetObdxEventSnapshotPersistencePort getObdxEventSnapshotPersistencePort;

    private GetEventSnapshotServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetEventSnapshotServiceCase(getObdxEventSnapshotPersistencePort);
    }

    @Test
    void returns_the_persisted_snapshot_when_present() {
        FetchClassificationDTO snapshot = new FetchClassificationDTO("evt-1", "Event", "FINISHED", "stage-1",
                "Stage A", "WC", "obdx", "cfg", "Cfg", null, null);
        when(getObdxEventSnapshotPersistencePort.getSnapshot("evt-1")).thenReturn(Optional.of(snapshot));

        assertThat(serviceCase.getSnapshot("evt-1", "obdx")).containsSame(snapshot);
    }

    @Test
    void returns_empty_when_there_is_no_snapshot() {
        when(getObdxEventSnapshotPersistencePort.getSnapshot("evt-1")).thenReturn(Optional.empty());

        assertThat(serviceCase.getSnapshot("evt-1", "obdx")).isEmpty();
    }
}
