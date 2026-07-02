package com.k9x.application.collections.obdx.use_case;

import com.k9x.application.collections.obdx.use_case.command.RegisterObdxRedCardCommand;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.events.obdx.exceptions.ObdxUserNotCollectorException;
import com.k9x.application.events.obdx.port.GetObdxEventCollectorPersistencePort;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.domain.events.exceptions.RedCardAlreadyRegisteredException;
import com.k9x.domain.events.valueobjects.Score;
import com.k9x.domain.stages.aggregates.StageSnapshot;
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
class RegisterObdxRedCardServiceCaseTest {

    private static final RegisterObdxRedCardCommand COMMAND = new RegisterObdxRedCardCommand(
            "judge-1", "OBDX_FCI_GRADE_3.1_V0", "dog-1");

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;
    @Mock
    private GetObdxEventCollectorPersistencePort getObdxEventCollectorPersistencePort;
    @Mock
    private SaveCompetitionPersistencePort saveCompetitionPersistencePort;
    private RegisterObdxRedCardServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new RegisterObdxRedCardServiceCase(getCompetitionPersistencePort,
                getObdxEventCollectorPersistencePort, saveCompetitionPersistencePort);
    }

    private CompetitionSnapshot competition() {
        EventSnapshot event = new EventSnapshot("event-1", null, null, "Event 1", "stage-1", "user-1", null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of());
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "user-1", 0L, Long.MAX_VALUE, 0L, 0L, null,
                List.of(event));
        return new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                0L, 0L, null, List.of(stage));
    }

    private CompetitionSnapshot competitionWithRedCardAlreadyRegistered() {
        EventSnapshot event = new EventSnapshot("event-1", null, null, "Event 1", "stage-1", "user-1", null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(),
                List.of(new Score("OBDX_FCI_GRADE_3.1_V0", "judge-1", "dog-1", null, 0L, null, 1000L)));
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "user-1", 0L, Long.MAX_VALUE, 0L, 0L, null,
                List.of(event));
        return new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                0L, 0L, null, List.of(stage));
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.registerRedCard("event-1", COMMAND, "user@k9x.io"))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(getObdxEventCollectorPersistencePort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_collector() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getObdxEventCollectorPersistencePort.getCollectorId("event-1", "judge-1")).thenReturn("other@k9x.io");

        assertThatThrownBy(() -> serviceCase.registerRedCard("event-1", COMMAND, "user@k9x.io"))
                .isInstanceOf(ObdxUserNotCollectorException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_red_card_already_registered() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getObdxEventCollectorPersistencePort.getCollectorId("event-1", "judge-1")).thenReturn("user@k9x.io");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competitionWithRedCardAlreadyRegistered());

        assertThatThrownBy(() -> serviceCase.registerRedCard("event-1", COMMAND, "user@k9x.io"))
                .isInstanceOf(RedCardAlreadyRegisteredException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void saves_aggregate_when_all_validations_pass() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getObdxEventCollectorPersistencePort.getCollectorId("event-1", "judge-1")).thenReturn("user@k9x.io");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition());

        serviceCase.registerRedCard("event-1", COMMAND, "user@k9x.io");

        verify(saveCompetitionPersistencePort).save(any());
    }
}
