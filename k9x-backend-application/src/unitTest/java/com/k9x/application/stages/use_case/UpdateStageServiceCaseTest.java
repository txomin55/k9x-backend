package com.k9x.application.stages.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.stages.use_case.command.UpdateStageCommand;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.competitions.aggregates.CompetitionSource;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import com.k9x.domain.stages.exceptions.StageNotFoundException;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateStageServiceCaseTest {

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;

    @Mock
    private SaveCompetitionPersistencePort saveCompetitionPersistencePort;

    private UpdateStageServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new UpdateStageServiceCase(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    private CompetitionSnapshot competition(String creator) {
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", creator,
                Long.MAX_VALUE, Long.MAX_VALUE, 0L, 0L, null, List.of());
        return new CompetitionSnapshot("comp-1", "World Cup", creator, "Org", "ES", "desc", "addr",
                null, null, CompetitionSource.API, null, 0L, 0L, null, List.of(stage));
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.updateStage("stage-1", new UpdateStageCommand("Stage 1", 1L, 2L), "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_stage_not_found() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateStage("stage-1", new UpdateStageCommand("Stage 1", 1L, 2L), "user-1", true))
                .isInstanceOf(StageNotFoundException.class);

        verify(getCompetitionPersistencePort, never()).getCompetition(any());
        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void saves_aggregate_when_all_validations_pass() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition("user-1"));

        serviceCase.updateStage("stage-1", new UpdateStageCommand("New Name", 1L, 2L), "user-1", true);

        verify(saveCompetitionPersistencePort).save(any());
    }
}
