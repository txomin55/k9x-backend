package com.k9x.application.events.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventCannotBeDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.port.DeleteObdxEventPersistencePort;
import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.events.EventCompetitor;
import com.k9x.domain.aggregates.events.EventExercise;
import com.k9x.domain.aggregates.events.EventJudge;
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
class DeleteEventServiceCaseTest {

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;

    @Mock
    private DeleteObdxEventPersistencePort deleteObdxEventPersistencePort;

    private DeleteEventServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new DeleteEventServiceCase(getCompetitionPersistencePort, deleteObdxEventPersistencePort);
    }

    private Event event(Long deletedAt, List<EventCompetitor> competitors, List<EventExercise> exercises,
                        List<EventJudge> judges, List<Score> scores) {
        return new Event("event-1", null, null, "Event 1", "stage-1", "user-1", 0L, 0L, deletedAt,
                ObdxAvgMethod.MID_AVG, competitors, exercises, judges, scores);
    }

    private Event createdEvent(Long deletedAt) {
        return event(deletedAt, List.of(), List.of(), List.of(), List.of());
    }

    private Competition competition(Event event, String stageCreator, Long stageDeletedAt) {
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", stageCreator, 0L, Long.MAX_VALUE, 0L, 0L,
                stageDeletedAt, List.of(event));
        return new Competition("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                0L, 0L, null, List.of(stage));
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.deleteEvent("event-1", "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getCompetitionPersistencePort, deleteObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.deleteEvent("event-1", "user-1", true))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(deleteObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_event_is_deleted() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(createdEvent(1700000000000L), "user-1", null));

        assertThatThrownBy(() -> serviceCase.deleteEvent("event-1", "user-1", true))
                .isInstanceOf(EventAlreadyDeletedException.class);

        verifyNoInteractions(deleteObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_event_is_started() {
        EventCompetitor competitor = new EventCompetitor("dog-1", "Rex", "owner", "team", "ES", "breed",
                "id-1", (short) 1, true, false);
        EventExercise exercise = new EventExercise("ex-1", (short) 1, List.of());
        EventJudge judge = new EventJudge("judge-1", "Judge", "collector@k9x.com");
        Score score = new Score("ex-1", "judge-1", "dog-1", new BigDecimal("7.5"), 1000L);
        Event started = event(null, List.of(competitor), List.of(exercise), List.of(judge), List.of(score));
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(started, "user-1", null));

        assertThatThrownBy(() -> serviceCase.deleteEvent("event-1", "user-1", true))
                .isInstanceOf(EventCannotBeDeletedException.class);

        verifyNoInteractions(deleteObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_event_is_finished() {
        // single competitor flagged notCompeting => settled => FINISHED (no scores needed)
        EventCompetitor competitor = new EventCompetitor("dog-1", "Rex", "owner", "team", "ES", "breed",
                "id-1", (short) 1, true, true);
        EventExercise exercise = new EventExercise("ex-1", (short) 1, List.of());
        EventJudge judge = new EventJudge("judge-1", "Judge", "collector@k9x.com");
        Event finished = event(null, List.of(competitor), List.of(exercise), List.of(judge), List.of());
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(finished, "user-1", null));

        assertThatThrownBy(() -> serviceCase.deleteEvent("event-1", "user-1", true))
                .isInstanceOf(EventCannotBeDeletedException.class);

        verifyNoInteractions(deleteObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_stage_is_deleted() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(createdEvent(null), "user-1", 1700000000000L));

        assertThatThrownBy(() -> serviceCase.deleteEvent("event-1", "user-1", true))
                .isInstanceOf(StageAlreadyDeletedException.class);

        verifyNoInteractions(deleteObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_stage_creator() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(createdEvent(null), "other-user", null));

        assertThatThrownBy(() -> serviceCase.deleteEvent("event-1", "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(deleteObdxEventPersistencePort);
    }

    @Test
    void deletes_event_when_all_validations_pass() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(createdEvent(null), "user-1", null));

        serviceCase.deleteEvent("event-1", "user-1", true);

        verify(deleteObdxEventPersistencePort).deleteEvent(eq("event-1"), anyLong());
    }
}
