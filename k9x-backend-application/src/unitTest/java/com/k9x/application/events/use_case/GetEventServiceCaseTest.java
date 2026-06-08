package com.k9x.application.events.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.dto.ConfigurationDTO;
import com.k9x.application.disciplines.use_case.dto.ConfigurationsDTO;
import com.k9x.application.disciplines.use_case.dto.ExerciseDTO;
import com.k9x.application.disciplines.use_case.dto.FederationInfoDTO;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.use_case.dto.FetchEventDetailDTO;
import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.events.EventCompetitor;
import com.k9x.domain.aggregates.events.EventExercise;
import com.k9x.domain.aggregates.events.EventJudge;
import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetEventServiceCaseTest {

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;
    @Mock
    private GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort;

    private GetEventServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetEventServiceCase(getCompetitionPersistencePort, getObdxFederationsConfigurationsPort);
    }

    private Event event() {
        return new Event("event-1", "cfg-1", "OBDX", "Event 1", "stage-1", "user-1", null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of());
    }

    private Event richEvent() {
        EventCompetitor competitor = new EventCompetitor("dog-1", "Rex", "owner", "team", "ES", "breed",
                "id-1", (short) 1, true, false);
        EventExercise exercise = new EventExercise("ex-1", (short) 1, List.of("tag-a", "tag-b"));
        EventJudge judge = new EventJudge("judge-1", "Judge", "collector@k9x.com");
        return new Event("event-1", "cfg-1", "OBDX", "Event 1", "stage-1", "user-1", null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(competitor), List.of(exercise), List.of(judge), List.of());
    }

    private Competition competition(Event event, String stageCreator, Long stageDeletedAt) {
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", stageCreator, 0L, Long.MAX_VALUE, 0L, 0L,
                stageDeletedAt, List.of(event));
        return new Competition("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                0L, 0L, null, List.of(stage));
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.getEvent("event-1", "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getCompetitionPersistencePort, getObdxFederationsConfigurationsPort);
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.getEvent("event-1", "user-1", true))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(getObdxFederationsConfigurationsPort);
    }

    @Test
    void throws_exception_when_stage_is_deleted() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(event(), "user-1", 1700000000000L));

        assertThatThrownBy(() -> serviceCase.getEvent("event-1", "user-1", true))
                .isInstanceOf(StageAlreadyDeletedException.class);
    }

    @Test
    void throws_exception_when_user_is_not_stage_creator() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(event(), "other-user", null));

        assertThatThrownBy(() -> serviceCase.getEvent("event-1", "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);
    }

    @Test
    void returns_obdx_event_detail_when_all_validations_pass() throws IOException {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(richEvent(), "user-1", null));
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenReturn(List.of(
                new ConfigurationsDTO(new FederationInfoDTO("fed-1", "Federation", "ES"),
                        List.of(new ConfigurationDTO("cfg-1", "Config 1", List.of(new ExerciseDTO("ex-1", "Exercise 1")))))));

        FetchEventDetailDTO result = serviceCase.getEvent("event-1", "user-1", true);

        assertThat(result.obdx()).isNotNull();
        assertThat(result.obdx().id()).isEqualTo("event-1");
        assertThat(result.obdx().discipline()).isEqualTo("OBDX");
        assertThat(result.competitors()).hasSize(1);
        assertThat(result.competitors().getFirst().status()).isEqualTo("ENROLLED");
        assertThat(result.judges()).hasSize(1);
        assertThat(result.exercises()).hasSize(1);
        assertThat(result.exercises().getFirst().name()).isEqualTo("Exercise 1");
        assertThat(result.exercises().getFirst().position()).isEqualTo(1);
        assertThat(result.exercises().getFirst().tags()).containsExactly("tag-a", "tag-b");
        assertThat(result.configuration()).isNotNull();
        assertThat(result.configuration().name()).isEqualTo("Config 1");
        assertThat(result.configuration().federation().id()).isEqualTo("fed-1");
    }
}
