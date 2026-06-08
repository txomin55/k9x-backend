package com.k9x.application.stages.use_case;

import com.k9x.application.competitions.exceptions.CompetitionAlreadyDeletedException;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.application.stages.exceptions.StageCannotBeDeletedException;
import com.k9x.application.stages.exceptions.StageNotFoundException;
import com.k9x.application.stages.port.DeleteStagePersistencePort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteStageServiceCaseTest {

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;

    @Mock
    private DeleteStagePersistencePort deleteStagePersistencePort;

    private DeleteStageServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new DeleteStageServiceCase(getCompetitionPersistencePort, deleteStagePersistencePort);
    }

    private Stage stage(String creator, Long deletedAt) {
        // dateFrom/dateTo in far future -> status CREATED (no events)
        return new Stage("stage-1", "Stage 1", "comp-1", creator,
                Long.MAX_VALUE, Long.MAX_VALUE, 0L, 0L, deletedAt, List.of());
    }

    private Competition competition(String creator, Long deletedAt, Stage stage) {
        return new Competition("comp-1", "World Cup", creator, "Org", "ES", "desc", "addr",
                null, null, 0L, 0L, deletedAt, List.of(stage));
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.deleteStage("stage-1", "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getCompetitionPersistencePort, deleteStagePersistencePort);
    }

    @Test
    void throws_exception_when_stage_not_found() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.deleteStage("stage-1", "user-1", true))
                .isInstanceOf(StageNotFoundException.class);

        verify(getCompetitionPersistencePort, never()).getCompetition(any());
        verifyNoInteractions(deleteStagePersistencePort);
    }

    @Test
    void throws_exception_when_stage_is_deleted() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition("user-1", null, stage("user-1", 1700000000000L)));

        assertThatThrownBy(() -> serviceCase.deleteStage("stage-1", "user-1", true))
                .isInstanceOf(StageAlreadyDeletedException.class);

        verifyNoInteractions(deleteStagePersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_stage_creator() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition("user-1", null, stage("other-user", null)));

        assertThatThrownBy(() -> serviceCase.deleteStage("stage-1", "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(deleteStagePersistencePort);
    }

    @Test
    void throws_exception_when_competition_is_deleted() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition("user-1", 1700000000000L, stage("user-1", null)));

        assertThatThrownBy(() -> serviceCase.deleteStage("stage-1", "user-1", true))
                .isInstanceOf(CompetitionAlreadyDeletedException.class);

        verifyNoInteractions(deleteStagePersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_competition_creator() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition("other-user", null, stage("user-1", null)));

        assertThatThrownBy(() -> serviceCase.deleteStage("stage-1", "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(deleteStagePersistencePort);
    }

    @Test
    void throws_exception_when_stage_is_started() {
        // An event with a recorded score makes the event STARTED, hence the stage STARTED.
        Event startedEvent = new Event("evt-1", "cfg-1", "obdx", "Open", "stage-1", "user-1",
                null, 0L, 0L, null, null,
                List.of(), List.of(), List.of(),
                List.of(new Score("ex-1", "judge-1", "dog-1", BigDecimal.TEN, 0L)));
        Stage startedStage = new Stage("stage-1", "Stage 1", "comp-1", "user-1",
                Long.MAX_VALUE, Long.MAX_VALUE, 0L, 0L, null, List.of(startedEvent));
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition("user-1", null, startedStage));

        assertThatThrownBy(() -> serviceCase.deleteStage("stage-1", "user-1", true))
                .isInstanceOf(StageCannotBeDeletedException.class);

        verifyNoInteractions(deleteStagePersistencePort);
    }

    @Test
    void throws_exception_when_stage_is_finished() {
        // dateTo = 0L (1970) means the stage day is strictly before today -> FINISHED.
        Stage finishedStage = new Stage("stage-1", "Stage 1", "comp-1", "user-1",
                0L, 0L, 0L, 0L, null, List.of());
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition("user-1", null, finishedStage));

        assertThatThrownBy(() -> serviceCase.deleteStage("stage-1", "user-1", true))
                .isInstanceOf(StageCannotBeDeletedException.class);

        verifyNoInteractions(deleteStagePersistencePort);
    }

    @Test
    void deletes_stage_when_status_is_created() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition("user-1", null, stage("user-1", null)));

        serviceCase.deleteStage("stage-1", "user-1", true);

        verify(deleteStagePersistencePort).deleteStage(eq("stage-1"), anyLong());
    }
}
