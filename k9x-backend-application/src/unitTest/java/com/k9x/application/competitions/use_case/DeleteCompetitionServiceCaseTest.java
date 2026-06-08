package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.exceptions.CompetitionAlreadyDeletedException;
import com.k9x.application.competitions.exceptions.CompetitionCannotBeDeletedException;
import com.k9x.application.competitions.exceptions.CompetitionNotFoundException;
import com.k9x.application.competitions.port.DeleteCompetitionPersistencePort;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.events.Score;
import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

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

    private Competition competition(String creator, Long deletedAt, List<Stage> stages) {
        return new Competition("comp-1", "World Cup", creator, "Org", "ES", "desc", "addr",
                null, null, 0L, 0L, deletedAt, stages);
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
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition("user-1", 1700000000000L, List.of()));

        assertThatThrownBy(() -> serviceCase.deleteCompetition("comp-1", "user-1", true))
                .isInstanceOf(CompetitionAlreadyDeletedException.class);

        verifyNoInteractions(deleteCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_creator() {
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition("other-user", null, List.of()));

        assertThatThrownBy(() -> serviceCase.deleteCompetition("comp-1", "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(deleteCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_competition_is_started() {
        // A started event -> started stage -> started competition.
        Event startedEvent = new Event("evt-1", "cfg-1", "obdx", "Open", "stage-1", "user-1",
                null, 0L, 0L, null, null,
                List.of(), List.of(), List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", BigDecimal.TEN, 0L)));
        Stage startedStage = new Stage("stage-1", "Stage 1", "comp-1", "user-1",
                Long.MAX_VALUE, Long.MAX_VALUE, 0L, 0L, null, List.of(startedEvent));
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition("user-1", null, List.of(startedStage)));

        assertThatThrownBy(() -> serviceCase.deleteCompetition("comp-1", "user-1", true))
                .isInstanceOf(CompetitionCannotBeDeletedException.class);

        verifyNoInteractions(deleteCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_competition_is_finished() {
        // dateTo = 0L (1970) -> finished stage -> finished competition.
        Stage finishedStage = new Stage("stage-1", "Stage 1", "comp-1", "user-1",
                0L, 0L, 0L, 0L, null, List.of());
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition("user-1", null, List.of(finishedStage)));

        assertThatThrownBy(() -> serviceCase.deleteCompetition("comp-1", "user-1", true))
                .isInstanceOf(CompetitionCannotBeDeletedException.class);

        verifyNoInteractions(deleteCompetitionPersistencePort);
    }

    @Test
    void deletes_competition_when_status_is_created() {
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition("user-1", null, List.of()));

        serviceCase.deleteCompetition("comp-1", "user-1", true);

        verify(deleteCompetitionPersistencePort).deleteCompetition(eq("comp-1"), anyLong());
    }
}
