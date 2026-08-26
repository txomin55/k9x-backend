package com.k9x.application.events.obdx.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.events.obdx.use_case.command.UpdateNotCompetingCommand;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.competitions.aggregates.CompetitionSource;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.exceptions.CompetitorAlreadyNotCompetingException;
import com.k9x.domain.events.exceptions.CompetitorNotFoundException;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.domain.events.valueobjects.EventCompetitor;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateNotCompetingServiceCaseTest {

    private static final UpdateNotCompetingCommand COMMAND = new UpdateNotCompetingCommand("dog-1", true);

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;
    @Mock
    private SaveCompetitionPersistencePort saveCompetitionPersistencePort;
    private UpdateNotCompetingServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new UpdateNotCompetingServiceCase(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    private CompetitionSnapshot competition(boolean notCompeting) {
        EventCompetitor competitor = new EventCompetitor("dog-1", "Rex", "Owner", "Handler", "Team", "ES", "Breed", null, null, null,
                (short) 1, null, true, notCompeting, null, null, null, null, null);
        EventSnapshot event = new EventSnapshot("event-1", null, null, "Event 1", "stage-1", "user-1", null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(competitor), List.of(), List.of(), List.of(), List.of(), null, null, null);
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "user-1", 0L, Long.MAX_VALUE, 0L, 0L, null,
                List.of(event));
        return new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                CompetitionSource.API, 0L, 0L, null, List.of(stage));
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.updateNotCompeting("event-1", COMMAND, "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateNotCompeting("event-1", COMMAND, "user-1", true))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_competitor_not_found() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition(false));

        assertThatThrownBy(() -> serviceCase.updateNotCompeting("event-1",
                new UpdateNotCompetingCommand("dog-missing", true), "user-1", true))
                .isInstanceOf(CompetitorNotFoundException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_competitor_is_already_not_competing() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition(true));

        assertThatThrownBy(() -> serviceCase.updateNotCompeting("event-1", COMMAND, "user-1", true))
                .isInstanceOf(CompetitorAlreadyNotCompetingException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_creator() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition(false));

        assertThatThrownBy(() -> serviceCase.updateNotCompeting("event-1", COMMAND, "intruder", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void saves_aggregate_when_all_validations_pass() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition(false));

        serviceCase.updateNotCompeting("event-1", COMMAND, "user-1", true);

        verify(saveCompetitionPersistencePort).save(any());
    }
}
