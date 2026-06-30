package com.k9x.application.stages.use_case;

import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.dto.ConfigurationDTO;
import com.k9x.application.disciplines.use_case.dto.ConfigurationsDTO;
import com.k9x.application.disciplines.use_case.dto.FederationInfoDTO;
import com.k9x.application.stages.port.GetStageListPersistencePort;
import com.k9x.application.stages.use_case.dto.FetchStageListDTO;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.disciplines.exceptions.DisciplineConfigurationMalformedException;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.valueobjects.EventCompetitor;
import com.k9x.domain.events.valueobjects.EventExercise;
import com.k9x.domain.events.valueobjects.EventJudge;
import com.k9x.domain.events.valueobjects.Score;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStageListServiceCaseTest {

    private static final long FAR_FUTURE = 4_000_000_000_000L; // year 2096
    private static final long FAR_PAST = 1_000L;               // 1970

    @Mock
    private GetStageListPersistencePort getStageListPersistencePort;

    @Mock
    private GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort;

    private GetStageListServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetStageListServiceCase(getStageListPersistencePort, getObdxFederationsConfigurationsPort);
    }

    @Test
    void resolves_discipline_name_and_computes_finished_stage_with_unscored_event() throws IOException {
        EventSnapshot event = event("evt-1", "obdx-1", null, List.of(competitor("dog-1", false)),
                List.of(exercise("ex-1")), List.of(judge("j-1")), List.of());
        CompetitionSnapshot competition = competition(stage("s-1", FAR_PAST, FAR_PAST, List.of(event)));

        when(getStageListPersistencePort.getCompetitions()).thenReturn(List.of(competition));
        when(getObdxFederationsConfigurationsPort.getConfigurations())
                .thenReturn(List.of(federation(new ConfigurationDTO("obdx-1", "Obedience", List.of()))));

        List<FetchStageListDTO> result = serviceCase.getStages();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().events()).hasSize(1);
        assertThat(result.getFirst().events().getFirst().disciplineName()).isEqualTo("Obedience");
        assertThat(result.getFirst().events().getFirst().competitorCount()).isEqualTo(1);
        // No scores -> event CREATED; dateTo in 1970 is before today's UTC day -> stage FINISHED.
        assertThat(result.getFirst().events().getFirst().status()).isEqualTo("CREATED");
        assertThat(result.getFirst().status()).isEqualTo("FINISHED");
    }

    @Test
    void surfaces_started_status_when_an_event_holds_a_score() throws IOException {
        // 2 exercises x 1 judge = 2 required scores, only 1 recorded -> competitor not settled (not FINISHED),
        // but a score exists -> event STARTED. dateTo in the far future rules out the date-driven FINISHED.
        EventSnapshot event = event("evt-1", "obdx-1", null, List.of(competitor("dog-1", false)),
                List.of(exercise("ex-1"), exercise("ex-2")), List.of(judge("j-1")),
                List.of(new Score("ex-1", "j-1", "dog-1", new BigDecimal("7.0"), 0L)));
        CompetitionSnapshot competition = competition(stage("s-1", FAR_PAST, FAR_FUTURE, List.of(event)));

        when(getStageListPersistencePort.getCompetitions()).thenReturn(List.of(competition));
        when(getObdxFederationsConfigurationsPort.getConfigurations())
                .thenReturn(List.of(federation(new ConfigurationDTO("obdx-1", "Obedience", List.of()))));

        List<FetchStageListDTO> result = serviceCase.getStages();

        assertThat(result.getFirst().events().getFirst().status()).isEqualTo("STARTED");
        assertThat(result.getFirst().status()).isEqualTo("STARTED");
    }

    @Test
    void uses_configuration_id_as_fallback_when_discipline_not_found() throws IOException {
        EventSnapshot event = event("evt-1", "unknown-id", null, List.of(), List.of(), List.of(), List.of());
        CompetitionSnapshot competition = competition(stage("s-1", FAR_PAST, FAR_PAST, List.of(event)));

        when(getStageListPersistencePort.getCompetitions()).thenReturn(List.of(competition));
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenReturn(List.of());

        List<FetchStageListDTO> result = serviceCase.getStages();

        assertThat(result.getFirst().events().getFirst().disciplineName()).isEqualTo("unknown-id");
    }

    @Test
    void skips_deleted_stages_and_deleted_events() throws IOException {
        EventSnapshot liveEvent = event("evt-1", "obdx-1", null, List.of(), List.of(), List.of(), List.of());
        EventSnapshot deletedEvent = event("evt-2", "obdx-1", 999L, List.of(), List.of(), List.of(), List.of());
        StageSnapshot liveStage = stage("s-1", FAR_PAST, FAR_PAST, List.of(liveEvent, deletedEvent));
        StageSnapshot deletedStage = new StageSnapshot("s-2", "Stage s-2", "comp", "creator",
                FAR_PAST, FAR_PAST, 0L, 0L, 888L, List.of(liveEvent));
        CompetitionSnapshot competition = competition(List.of(liveStage, deletedStage));

        when(getStageListPersistencePort.getCompetitions()).thenReturn(List.of(competition));
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenReturn(List.of());

        List<FetchStageListDTO> result = serviceCase.getStages();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo("s-1");
        assertThat(result.getFirst().events()).extracting(e -> e.id()).containsExactly("evt-1");
    }

    @Test
    void throws_when_configurations_cannot_be_loaded() throws IOException {
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenThrow(new IOException());

        assertThatThrownBy(() -> serviceCase.getStages())
                .isInstanceOf(DisciplineConfigurationMalformedException.class);
    }

    private static ConfigurationsDTO federation(ConfigurationDTO config) {
        return new ConfigurationsDTO(new FederationInfoDTO("FED", "Federation", "ES"), List.of(config));
    }

    private static CompetitionSnapshot competition(StageSnapshot stage) {
        return competition(List.of(stage));
    }

    private static CompetitionSnapshot competition(List<StageSnapshot> stages) {
        return new CompetitionSnapshot("comp", "Comp", "creator", "Organizer Name", "ES",
                "desc", "Calle Mayor 1", 40.4, -3.7, 0L, 0L, null, stages);
    }

    private static StageSnapshot stage(String id, long from, long to, List<EventSnapshot> events) {
        return new StageSnapshot(id, "Stage " + id, "comp", "creator", from, to, 0L, 0L, null, events);
    }

    private static EventSnapshot event(String id, String configId, Long deletedAt, List<EventCompetitor> competitors,
                                       List<EventExercise> exercises, List<EventJudge> judges, List<Score> scores) {
        return new EventSnapshot(id, configId, "OBDX", "Event " + id, "s-1", "creator",
                null, 0L, 0L, deletedAt, ObdxAvgMethod.AVG, competitors, exercises, judges, scores);
    }

    private static EventCompetitor competitor(String dogId, boolean notCompeting) {
        return new EventCompetitor(dogId, "Rex", "owner", "Handler", "Team A", "ES", "Border Collie", "ID-001",
                (short) 1, true, notCompeting);
    }

    private static EventExercise exercise(String id) {
        return new EventExercise(id, (short) 1, List.of());
    }

    private static EventJudge judge(String id) {
        return new EventJudge(id, "Judge " + id, "collector@test.com");
    }
}
