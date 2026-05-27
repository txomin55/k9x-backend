package com.k9x.application.stages.use_case;

import com.k9x.application.competitions.exceptions.CompetitionAlreadyDeletedException;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.application.stages.exceptions.StageNotFoundException;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.application.stages.port.UpdateStagePersistencePort;
import com.k9x.application.stages.use_case.command.UpdateStageCommand;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateStageServiceCaseTest {

    @Mock
    private GetStagePersistencePort getStagePersistencePort;

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;

    @Mock
    private UpdateStagePersistencePort updateStagePersistencePort;

    private UpdateStageServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new UpdateStageServiceCase(getStagePersistencePort, getCompetitionPersistencePort, updateStagePersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.updateStage("stage-1", new UpdateStageCommand("Stage 1", 1L, 2L), "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getStagePersistencePort, getCompetitionPersistencePort, updateStagePersistencePort);
    }

    @Test
    void throws_exception_when_stage_not_found() {
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateStage("stage-1", new UpdateStageCommand("Stage 1", 1L, 2L), "user-1", true))
                .isInstanceOf(StageNotFoundException.class);

        verifyNoInteractions(getCompetitionPersistencePort, updateStagePersistencePort);
    }

    @Test
    void throws_exception_when_stage_is_deleted() {
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE, 0L, 0L, 1700000000000L);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);

        assertThatThrownBy(() -> serviceCase.updateStage("stage-1", new UpdateStageCommand("Stage 1", 1L, 2L), "user-1", true))
                .isInstanceOf(StageAlreadyDeletedException.class);

        verifyNoInteractions(getCompetitionPersistencePort, updateStagePersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_creator() {
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "other-user", Long.MAX_VALUE, 0L, 0L, null);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);

        assertThatThrownBy(() -> serviceCase.updateStage("stage-1", new UpdateStageCommand("Stage 1", 1L, 2L), "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getCompetitionPersistencePort, updateStagePersistencePort);
    }

    @Test
    void throws_exception_when_competition_is_deleted() {
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE, 0L, 0L, null);
        Competition deletedCompetition = new Competition("comp-1", "World Cup", "user-1", 0L, 0L, 1700000000000L);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(deletedCompetition);

        assertThatThrownBy(() -> serviceCase.updateStage("stage-1", new UpdateStageCommand("Stage 1", 1L, 2L), "user-1", true))
                .isInstanceOf(CompetitionAlreadyDeletedException.class);

        verifyNoInteractions(updateStagePersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_competition_creator() {
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE, 0L, 0L, null);
        Competition competition = new Competition("comp-1", "World Cup", "other-user", 0L, 0L, null);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition);

        assertThatThrownBy(() -> serviceCase.updateStage("stage-1", new UpdateStageCommand("Stage 1", 1L, 2L), "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(updateStagePersistencePort);
    }

    @Test
    void updates_stage_when_all_validations_pass() {
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE, 0L, 0L, null);
        Competition competition = new Competition("comp-1", "World Cup", "user-1", 0L, 0L, null);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(stage);
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition);

        serviceCase.updateStage("stage-1", new UpdateStageCommand("New Name", 1L, 2L), "user-1", true);

        verify(updateStagePersistencePort).updateStage(eq("stage-1"), any());
    }
}
