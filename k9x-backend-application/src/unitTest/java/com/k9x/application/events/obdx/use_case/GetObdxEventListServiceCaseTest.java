package com.k9x.application.events.obdx.use_case;

import com.k9x.application.events.obdx.port.GetObdxEventListPersistencePort;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventDTO;
import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.application.stages.exceptions.StageNotFoundException;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetObdxEventListServiceCaseTest {

    @Mock
    private GetStagePersistencePort getStagePersistencePort;

    @Mock
    private GetObdxEventListPersistencePort getObdxEventListPersistencePort;

    private GetObdxEventListServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetObdxEventListServiceCase(getStagePersistencePort, getObdxEventListPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.getEvents(List.of("stage-1"), "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getStagePersistencePort, getObdxEventListPersistencePort);
    }

    @Test
    void throws_exception_when_stage_not_found() {
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.getEvents(List.of("stage-1"), "user-1", true))
                .isInstanceOf(StageNotFoundException.class);

        verifyNoInteractions(getObdxEventListPersistencePort);
    }

    @Test
    void throws_exception_when_stage_is_deleted() {
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE, 0L, 0L, 1700000000000L);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);

        assertThatThrownBy(() -> serviceCase.getEvents(List.of("stage-1"), "user-1", true))
                .isInstanceOf(StageAlreadyDeletedException.class);

        verifyNoInteractions(getObdxEventListPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_stage_creator() {
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "other-user", Long.MAX_VALUE, 0L, 0L, null);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);

        assertThatThrownBy(() -> serviceCase.getEvents(List.of("stage-1"), "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getObdxEventListPersistencePort);
    }

    @Test
    void returns_events_when_all_validations_pass() {
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE, 0L, 0L, null);
        List<FetchObdxEventDTO> events = List.of(new FetchObdxEventDTO("event-1", "Event 1", "stage-1", "Stage 1"));
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);
        when(getObdxEventListPersistencePort.getEvents(List.of("stage-1"))).thenReturn(events);

        List<FetchObdxEventDTO> result = serviceCase.getEvents(List.of("stage-1"), "user-1", true);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo("event-1");
        verify(getObdxEventListPersistencePort).getEvents(List.of("stage-1"));
    }
}
