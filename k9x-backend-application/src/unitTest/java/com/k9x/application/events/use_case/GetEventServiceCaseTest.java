package com.k9x.application.events.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.dto.ConfigurationDTO;
import com.k9x.application.disciplines.use_case.dto.ConfigurationsDTO;
import com.k9x.application.disciplines.use_case.dto.ExerciseDTO;
import com.k9x.application.disciplines.use_case.dto.FederationInfoDTO;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.application.events.use_case.dto.FetchEventDetailDTO;
import com.k9x.domain.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.valueobjects.EventCompetitor;
import com.k9x.domain.events.valueobjects.EventExercise;
import com.k9x.domain.events.valueobjects.EventJudge;
import com.k9x.domain.stages.aggregates.StageSnapshot;
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

    private EventSnapshot event() {
        return new EventSnapshot("event-1", "cfg-1", "OBDX", "Event 1", "stage-1", "user-1", null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of());
    }

    private EventSnapshot richEvent() {
        EventCompetitor competitor = new EventCompetitor("dog-1", "Rex", "owner", "Handler", "team", "ES", "breed",
                "id-1", (short) 1, true, false, null, null);
        EventExercise exercise = new EventExercise("ex-1", (short) 1, List.of("tag-a", "tag-b"));
        EventJudge judge = new EventJudge("judge-1", "Judge", "collector@k9x.com");
        return new EventSnapshot("event-1", "cfg-1", "OBDX", "Event 1", "stage-1", "user-1", null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(competitor), List.of(exercise), List.of(judge), List.of());
    }

    private CompetitionSnapshot competition(EventSnapshot event, String stageCreator, Long stageDeletedAt) {
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", stageCreator, 0L, Long.MAX_VALUE, 0L, 0L,
                stageDeletedAt, List.of(event));
        return new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
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
        assertThat(result.obdx().scoreCalculation()).isEqualTo(ObdxAvgMethod.MID_AVG);
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

    @Test
    void exposes_enrollment_deadline_and_domain_status_in_obdx_detail() throws IOException {
        EventSnapshot event = new EventSnapshot("event-1", "cfg-1", "OBDX", "Event 1", "stage-1", "user-1",
                1700000000000L, 0L, 0L, null, ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of());
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(event, "user-1", null));
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenReturn(List.of());

        FetchEventDetailDTO result = serviceCase.getEvent("event-1", "user-1", true);

        assertThat(result.obdx().enrollmentDeadline()).isEqualTo(1700000000000L);
        // no scores recorded -> domain-computed status is CREATED
        assertThat(result.obdx().status()).isEqualTo("CREATED");
    }

    @Test
    void marks_competitor_as_not_competing_when_flagged() throws IOException {
        EventCompetitor competitor = new EventCompetitor("dog-1", "Rex", "owner", "Handler", "team", "ES", "breed",
                "id-1", (short) 1, true, true, null, null);
        EventSnapshot event = new EventSnapshot("event-1", "cfg-1", "OBDX", "Event 1", "stage-1", "user-1", null, 0L,
                0L, null, ObdxAvgMethod.MID_AVG, List.of(competitor), List.of(), List.of(), List.of());
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(event, "user-1", null));
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenReturn(List.of());

        FetchEventDetailDTO result = serviceCase.getEvent("event-1", "user-1", true);

        assertThat(result.competitors()).hasSize(1);
        assertThat(result.competitors().getFirst().status()).isEqualTo("NOT_COMPETING");
    }

    @Test
    void marks_competitor_as_pending_enroll_accept_when_not_verified() throws IOException {
        EventCompetitor competitor = new EventCompetitor("dog-1", "Rex", "owner", "Handler", "team", "ES", "breed",
                "id-1", (short) 1, false, false, null, null);
        EventSnapshot event = new EventSnapshot("event-1", "cfg-1", "OBDX", "Event 1", "stage-1", "user-1", null, 0L,
                0L, null, ObdxAvgMethod.MID_AVG, List.of(competitor), List.of(), List.of(), List.of());
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competition(event, "user-1", null));
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenReturn(List.of());

        FetchEventDetailDTO result = serviceCase.getEvent("event-1", "user-1", true);

        assertThat(result.competitors()).hasSize(1);
        assertThat(result.competitors().getFirst().status()).isEqualTo("PENDING_ENROLL_ACCEPT");
    }
}
