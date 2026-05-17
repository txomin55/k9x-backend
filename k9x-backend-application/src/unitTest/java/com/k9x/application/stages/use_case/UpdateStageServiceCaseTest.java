package com.k9x.application.stages.use_case;

import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.application.stages.exceptions.StageNotFoundException;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.application.stages.port.UpdateStagePersistencePort;
import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateStageServiceCaseTest {

    @Mock
    private GetStagePersistencePort getStagePersistencePort;

    @Mock
    private UpdateStagePersistencePort updateStagePersistencePort;

    private UpdateStageServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new UpdateStageServiceCase(getStagePersistencePort, updateStagePersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.updateStage("stage-1", "Stage 1", 1L, 2L, "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getStagePersistencePort, updateStagePersistencePort);
    }

    @Test
    void throws_exception_when_stage_not_found() {
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateStage("stage-1", "Stage 1", 1L, 2L, "user-1", true))
                .isInstanceOf(StageNotFoundException.class);

        verifyNoInteractions(updateStagePersistencePort);
    }

    @Test
    void throws_exception_when_stage_is_deleted() {
        Stage stage = new Stage("stage-1", "Stage 1", "user-1", 0L, 0L, 1700000000000L);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);

        assertThatThrownBy(() -> serviceCase.updateStage("stage-1", "Stage 1", 1L, 2L, "user-1", true))
                .isInstanceOf(StageAlreadyDeletedException.class);

        verifyNoInteractions(updateStagePersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_creator() {
        Stage stage = new Stage("stage-1", "Stage 1", "other-user", 0L, 0L, null);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);

        assertThatThrownBy(() -> serviceCase.updateStage("stage-1", "Stage 1", 1L, 2L, "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(updateStagePersistencePort);
    }

    @Test
    void updates_stage_when_all_validations_pass() {
        Stage stage = new Stage("stage-1", "Stage 1", "user-1", 0L, 0L, null);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);

        serviceCase.updateStage("stage-1", "New Name", 1L, 2L, "user-1", true);

        verify(updateStagePersistencePort).updateStage(eq("stage-1"), eq("New Name"), eq(1L), eq(2L), anyLong());
    }
}
