package com.k9x.application.events.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.events.obdx.use_case.port.CreateObdxEventPersistencePort;
import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.application.stages.exceptions.StageNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateEventServiceCaseTest {

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;

    @Mock
    private CreateObdxEventPersistencePort createObdxEventPersistencePort;

    private CreateEventServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new CreateEventServiceCase(getCompetitionPersistencePort, createObdxEventPersistencePort);
    }

    private Competition competitionWithStage(Stage stage) {
        return new Competition("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                0L, 0L, null, List.of(stage));
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.createEvent("event-1", "Event 1", "stage-1", "obdx", "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getCompetitionPersistencePort, createObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_stage_not_found() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.createEvent("event-1", "Event 1", "stage-1", "obdx", "user-1", true))
                .isInstanceOf(StageNotFoundException.class);

        verifyNoInteractions(createObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_stage_is_deleted() {
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE, 0L, 0L, 0L, 1700000000000L, List.of());
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competitionWithStage(stage));

        assertThatThrownBy(() -> serviceCase.createEvent("event-1", "Event 1", "stage-1", "obdx", "user-1", true))
                .isInstanceOf(StageAlreadyDeletedException.class);

        verifyNoInteractions(createObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_stage_creator() {
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "other-user", Long.MAX_VALUE, 0L, 0L, 0L, null, List.of());
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competitionWithStage(stage));

        assertThatThrownBy(() -> serviceCase.createEvent("event-1", "Event 1", "stage-1", "obdx", "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(createObdxEventPersistencePort);
    }

    @Test
    void creates_event_when_all_conditions_are_met() {
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE, 0L, 0L, 0L, null, List.of());
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competitionWithStage(stage));

        serviceCase.createEvent("event-1", "Event 1", "stage-1", "obdx", "user-1", true);

        verify(createObdxEventPersistencePort).createEvent(eq("event-1"), eq("Event 1"), eq("stage-1"), eq("obdx"), eq("user-1"), anyLong());
    }
}
