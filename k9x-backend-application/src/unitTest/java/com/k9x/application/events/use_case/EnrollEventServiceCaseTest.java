package com.k9x.application.events.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.events.obdx.exceptions.BihNotAllowedForSexException;
import com.k9x.application.events.obdx.use_case.command.EnrollObdxEventCommand;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.domain.dogs.aggregates.Sex;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import com.k9x.domain.events.exceptions.EnrollmentClosedException;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollEventServiceCaseTest {

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;

    @Mock
    private SaveCompetitionPersistencePort saveCompetitionPersistencePort;

    @Mock
    private GetDogPersistencePort getDogPersistencePort;

    private EnrollEventServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new EnrollEventServiceCase(getCompetitionPersistencePort, saveCompetitionPersistencePort, getDogPersistencePort);
    }

    private CompetitionSnapshot competition() {
        EventSnapshot event = new EventSnapshot("event-1", null, null, "Event 1", "stage-1", "user-1",
                Long.MAX_VALUE, 0L, 0L, null, ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), List.of(), null);
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE,
                Long.MAX_VALUE, 0L, 0L, null, List.of(event));
        return new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                0L, 0L, null, List.of(stage));
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.enrollEvent("event-1", new EnrollObdxEventCommand("dog-1", false), "user-1"))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void saves_aggregate_when_all_validations_pass() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition());

        serviceCase.enrollEvent("event-1", new EnrollObdxEventCommand("dog-1", false), "user-1");

        verify(saveCompetitionPersistencePort).save(any());
    }

    @Test
    void throws_exception_when_bih_true_for_male_dog() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getDogPersistencePort.getDog("dog-1"))
                .thenReturn(new Dog("dog-1", "id", "breed", "Rex", "img", "owner-1", "handler-1", "creator-1", "ES", "team",
                        Sex.MALE, 55, null, 0L, 0L, null));

        assertThatThrownBy(() -> serviceCase.enrollEvent("event-1", new EnrollObdxEventCommand("dog-1", true), "user-1"))
                .isInstanceOf(BihNotAllowedForSexException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_event_has_no_enrollment_deadline() {
        EventSnapshot event = new EventSnapshot("event-1", null, null, "Event 1", "stage-1", "user-1", null, 0L, 0L,
                null, ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), List.of(), null);
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE,
                Long.MAX_VALUE, 0L, 0L, null, List.of(event));
        CompetitionSnapshot competition = new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", null, null, null,
                null, null, 0L, 0L, null, List.of(stage));
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition);

        assertThatThrownBy(() -> serviceCase.enrollEvent("event-1", new EnrollObdxEventCommand("dog-1", false), "user-1"))
                .isInstanceOf(EnrollmentClosedException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void saves_aggregate_when_bih_true_for_female_dog() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition());
        when(getDogPersistencePort.getDog("dog-1"))
                .thenReturn(new Dog("dog-1", "id", "breed", "Rex", "img", "owner-1", "handler-1", "creator-1", "ES", "team",
                        Sex.FEMALE, 55, null, 0L, 0L, null));

        serviceCase.enrollEvent("event-1", new EnrollObdxEventCommand("dog-1", true), "user-1");

        verify(saveCompetitionPersistencePort).save(any());
    }
}
