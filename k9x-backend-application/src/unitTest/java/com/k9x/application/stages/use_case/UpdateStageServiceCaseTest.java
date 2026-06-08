package com.k9x.application.stages.use_case;

import com.k9x.application.competitions.exceptions.CompetitionAlreadyDeletedException;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.application.stages.exceptions.StageNotFoundException;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateStageServiceCaseTest {

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;

    @Mock
    private UpdateStagePersistencePort updateStagePersistencePort;

    private UpdateStageServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new UpdateStageServiceCase(getCompetitionPersistencePort, updateStagePersistencePort);
    }

    private Stage stage(String creator, Long deletedAt) {
        return new Stage("stage-1", "Stage 1", "comp-1", creator,
                Long.MAX_VALUE, Long.MAX_VALUE, 0L, 0L, deletedAt, List.of());
    }

    private Competition competition(String creator, Long deletedAt, Stage stage) {
        return new Competition("comp-1", "World Cup", creator, "Org", "ES", "desc", "addr",
                null, null, 0L, 0L, deletedAt, List.of(stage));
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.updateStage("stage-1", new UpdateStageCommand("Stage 1", 1L, 2L), "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getCompetitionPersistencePort, updateStagePersistencePort);
    }

    @Test
    void throws_exception_when_stage_not_found() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateStage("stage-1", new UpdateStageCommand("Stage 1", 1L, 2L), "user-1", true))
                .isInstanceOf(StageNotFoundException.class);

        verify(getCompetitionPersistencePort, never()).getCompetition(any());
        verifyNoInteractions(updateStagePersistencePort);
    }

    @Test
    void throws_exception_when_stage_is_deleted() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition("user-1", null, stage("user-1", 1700000000000L)));

        assertThatThrownBy(() -> serviceCase.updateStage("stage-1", new UpdateStageCommand("Stage 1", 1L, 2L), "user-1", true))
                .isInstanceOf(StageAlreadyDeletedException.class);

        verifyNoInteractions(updateStagePersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_creator() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition("user-1", null, stage("other-user", null)));

        assertThatThrownBy(() -> serviceCase.updateStage("stage-1", new UpdateStageCommand("Stage 1", 1L, 2L), "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(updateStagePersistencePort);
    }

    @Test
    void throws_exception_when_competition_is_deleted() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition("user-1", 1700000000000L, stage("user-1", null)));

        assertThatThrownBy(() -> serviceCase.updateStage("stage-1", new UpdateStageCommand("Stage 1", 1L, 2L), "user-1", true))
                .isInstanceOf(CompetitionAlreadyDeletedException.class);

        verifyNoInteractions(updateStagePersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_competition_creator() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition("other-user", null, stage("user-1", null)));

        assertThatThrownBy(() -> serviceCase.updateStage("stage-1", new UpdateStageCommand("Stage 1", 1L, 2L), "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(updateStagePersistencePort);
    }

    @Test
    void updates_stage_when_all_validations_pass() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition("user-1", null, stage("user-1", null)));

        serviceCase.updateStage("stage-1", new UpdateStageCommand("New Name", 1L, 2L), "user-1", true);

        verify(updateStagePersistencePort).updateStage(eq("stage-1"), any());
    }
}
