package com.k9x.application.events.obdx.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.events.exceptions.EventConfigurationIdRequiredException;
import com.k9x.application.events.obdx.exceptions.BihNotAllowedForSexException;
import com.k9x.application.events.obdx.exceptions.ObdxCollectorNotFoundException;
import com.k9x.application.events.obdx.use_case.command.UpdateObdxEventCommand;
import com.k9x.application.users.port.GetUserInfoPersistencePort;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.domain.dogs.aggregates.Sex;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateObdxEventServiceCaseTest {

    private static final UpdateObdxEventCommand VALID_COMMAND = new UpdateObdxEventCommand(
            "Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of());

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;
    @Mock
    private SaveCompetitionPersistencePort saveCompetitionPersistencePort;
    @Mock
    private GetUserInfoPersistencePort getUserInfoPersistencePort;
    @Mock
    private GetDogPersistencePort getDogPersistencePort;
    private UpdateObdxEventServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new UpdateObdxEventServiceCase(getCompetitionPersistencePort, saveCompetitionPersistencePort,
                getUserInfoPersistencePort, getDogPersistencePort);
    }

    private CompetitionSnapshot competition() {
        EventSnapshot event = new EventSnapshot("event-1", null, null, "Event 1", "stage-1", "user-1", null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of());
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "user-1", 0L, Long.MAX_VALUE, 0L, 0L, null,
                List.of(event));
        return new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                0L, 0L, null, List.of(stage));
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", VALID_COMMAND, "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_configuration_id_is_blank() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "  ", 1735689600000L, ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of());

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(EventConfigurationIdRequiredException.class);

        verifyNoInteractions(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", VALID_COMMAND, "user-1", true))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_collector_email_does_not_exist() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG,
                List.of(), List.of(), List.of(new UpdateObdxEventCommand.JudgeCommand("judge-1", "missing@k9x.com")));
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getUserInfoPersistencePort.findById("missing@k9x.com")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(ObdxCollectorNotFoundException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void saves_aggregate_when_all_validations_pass() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition());

        serviceCase.updateEvent("event-1", VALID_COMMAND, "user-1", true);

        verify(saveCompetitionPersistencePort).save(any());
    }

    @Test
    void throws_exception_when_bih_true_for_male_dog() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG,
                List.of(new UpdateObdxEventCommand.CompetitorCommand("dog-1", 1, true)), List.of(), List.of());
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getDogPersistencePort.getDog("dog-1"))
                .thenReturn(new Dog("dog-1", "id", "breed", "Rex", "img", "owner-1", "handler-1", "creator-1", "ES", "team",
                        Sex.MALE, 55, 0L, 0L, null));

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(BihNotAllowedForSexException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }
}
