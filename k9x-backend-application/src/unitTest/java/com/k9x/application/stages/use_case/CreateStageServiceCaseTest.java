package com.k9x.application.stages.use_case;

import com.k9x.application.competitions.exceptions.CompetitionAlreadyDeletedException;
import com.k9x.application.competitions.exceptions.CompetitionNotFoundException;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.stages.port.CreateStagePersistencePort;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateStageServiceCaseTest {

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;

    @Mock
    private CreateStagePersistencePort createStagePersistencePort;

    private CreateStageServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new CreateStageServiceCase(getCompetitionPersistencePort, createStagePersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.createStage("stage-1", "Stage 1", "comp-1", 1L, 2L, "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getCompetitionPersistencePort, createStagePersistencePort);
    }

    @Test
    void throws_exception_when_competition_does_not_exist() {
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.createStage("stage-1", "Stage 1", "comp-1", 1L, 2L, "user-1", true))
                .isInstanceOf(CompetitionNotFoundException.class);

        verifyNoInteractions(createStagePersistencePort);
    }

    @Test
    void throws_exception_when_competition_is_deleted() {
        Competition deleted = new Competition("comp-1", "World Cup", "user-1", 0L, 0L, 1700000000000L);
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(deleted);

        assertThatThrownBy(() -> serviceCase.createStage("stage-1", "Stage 1", "comp-1", 1L, 2L, "user-1", true))
                .isInstanceOf(CompetitionAlreadyDeletedException.class);

        verifyNoInteractions(createStagePersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_competition_creator() {
        Competition competition = new Competition("comp-1", "World Cup", "other-user", 0L, 0L, null);
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition);

        assertThatThrownBy(() -> serviceCase.createStage("stage-1", "Stage 1", "comp-1", 1L, 2L, "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(createStagePersistencePort);
    }

    @Test
    void creates_stage_when_all_conditions_are_met() {
        Competition competition = new Competition("comp-1", "World Cup", "user-1", 0L, 0L, null);
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition);

        serviceCase.createStage("stage-1", "Stage 1", "comp-1", 1L, 2L, "user-1", true);

        verify(createStagePersistencePort).createStage(eq("stage-1"), eq("Stage 1"), eq("comp-1"), eq(1L), eq(2L), eq("user-1"), anyLong());
    }
}
