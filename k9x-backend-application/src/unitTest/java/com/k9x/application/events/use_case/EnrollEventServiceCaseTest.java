package com.k9x.application.events.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.port.EnrollObdxEventPersistencePort;
import com.k9x.application.events.obdx.use_case.command.EnrollObdxEventCommand;
import com.k9x.application.stages.exceptions.StageExpiredException;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.stages.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollEventServiceCaseTest {

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;

    @Mock
    private EnrollObdxEventPersistencePort enrollObdxEventPersistencePort;

    private EnrollEventServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new EnrollEventServiceCase(getCompetitionPersistencePort, enrollObdxEventPersistencePort);
    }

    private Event event(Long deletedAt) {
        return new Event("event-1", null, null, "Event 1", "stage-1", "user-1", 0L, 0L, deletedAt,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of());
    }

    private Competition competition(Event event, long dateTo) {
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "user-1", 0L, dateTo, 0L, 0L, null, List.of(event));
        return new Competition("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                0L, 0L, null, List.of(stage));
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.enrollEvent("event-1", new EnrollObdxEventCommand("dog-1")))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(enrollObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_event_is_deleted() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(event(1700000000000L), Long.MAX_VALUE));

        assertThatThrownBy(() -> serviceCase.enrollEvent("event-1", new EnrollObdxEventCommand("dog-1")))
                .isInstanceOf(EventAlreadyDeletedException.class);

        verifyNoInteractions(enrollObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_stage_is_expired() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(event(null), 1L));

        assertThatThrownBy(() -> serviceCase.enrollEvent("event-1", new EnrollObdxEventCommand("dog-1")))
                .isInstanceOf(StageExpiredException.class);

        verifyNoInteractions(enrollObdxEventPersistencePort);
    }

    @Test
    void enrolls_event_when_all_validations_pass() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(event(null), Long.MAX_VALUE));

        serviceCase.enrollEvent("event-1", new EnrollObdxEventCommand("dog-1"));

        verify(enrollObdxEventPersistencePort).enrollEvent(eq("event-1"), any());
    }
}
