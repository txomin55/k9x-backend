package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.exceptions.CompetitionAlreadyDeletedException;
import com.k9x.application.competitions.exceptions.CompetitionNotFoundException;
import com.k9x.application.competitions.port.DeleteCompetitionPersistencePort;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
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
class DeleteCompetitionServiceCaseTest {

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;

    @Mock
    private DeleteCompetitionPersistencePort deleteCompetitionPersistencePort;

    private DeleteCompetitionServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new DeleteCompetitionServiceCase(getCompetitionPersistencePort, deleteCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.deleteCompetition("comp-1", "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getCompetitionPersistencePort, deleteCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_competition_does_not_exist() {
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.deleteCompetition("comp-1", "user-1", true))
                .isInstanceOf(CompetitionNotFoundException.class);

        verifyNoInteractions(deleteCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_competition_is_deleted() {
        Competition competition = new Competition("comp-1", "World Cup", "user-1", 0L, 0L, 1700000000000L);
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition);

        assertThatThrownBy(() -> serviceCase.deleteCompetition("comp-1", "user-1", true))
                .isInstanceOf(CompetitionAlreadyDeletedException.class);

        verifyNoInteractions(deleteCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_creator() {
        Competition competition = new Competition("comp-1", "World Cup", "other-user", 0L, 0L, null);
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition);

        assertThatThrownBy(() -> serviceCase.deleteCompetition("comp-1", "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(deleteCompetitionPersistencePort);
    }

    @Test
    void deletes_competition_when_all_validations_pass() {
        Competition competition = new Competition("comp-1", "World Cup", "user-1", 0L, 0L, null);
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition);

        serviceCase.deleteCompetition("comp-1", "user-1", true);

        verify(deleteCompetitionPersistencePort).deleteCompetition(eq("comp-1"), anyLong());
    }
}
