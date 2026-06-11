package com.k9x.application.stages.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
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
class DeleteStageServiceCaseTest {

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;

    @Mock
    private SaveCompetitionPersistencePort saveCompetitionPersistencePort;

    private DeleteStageServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new DeleteStageServiceCase(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    private CompetitionSnapshot competition(String creator) {
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", creator,
                Long.MAX_VALUE, Long.MAX_VALUE, 0L, 0L, null, List.of());
        return new CompetitionSnapshot("comp-1", "World Cup", creator, "Org", "ES", "desc", "addr",
                null, null, 0L, 0L, null, List.of(stage));
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.deleteStage("stage-1", "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_stage_not_found() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.deleteStage("stage-1", "user-1", true))
                .isInstanceOf(StageNotFoundException.class);

        verify(getCompetitionPersistencePort, never()).getCompetition(any());
        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void saves_aggregate_when_status_is_created() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition("user-1"));

        serviceCase.deleteStage("stage-1", "user-1", true);

        verify(saveCompetitionPersistencePort).save(any());
    }
}
