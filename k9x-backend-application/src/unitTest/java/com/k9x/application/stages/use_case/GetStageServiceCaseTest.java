package com.k9x.application.stages.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.notifications.port.GetStageNotificationsPersistencePort;
import com.k9x.application.disciplines.use_case.dto.ConfigurationDTO;
import com.k9x.application.disciplines.use_case.dto.ConfigurationsDTO;
import com.k9x.application.disciplines.use_case.dto.FederationInfoDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailDTO;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.disciplines.exceptions.DisciplineConfigurationMalformedException;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import com.k9x.domain.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.domain.stages.exceptions.StageNotFoundException;
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
class GetStageServiceCaseTest {

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;

    @Mock
    private GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort;

    @Mock
    private GetStageNotificationsPersistencePort getStageNotificationsPersistencePort;

    private GetStageServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetStageServiceCase(getCompetitionPersistencePort, getObdxFederationsConfigurationsPort,
                getStageNotificationsPersistencePort);
    }

    private EventSnapshot event() {
        return new EventSnapshot("evt-1", "obdx-1", "obdx", "Open", "s-1", "user-1",
                null, 0L, 0L, null, null, List.of(), List.of(), List.of(), List.of(), List.of(), null, null, null, null);
    }

    private CompetitionSnapshot competition(StageSnapshot stage) {
        return new CompetitionSnapshot("comp-1", "World Cup", "user-1", "Organizer", "ES", "desc", "Calle Mayor 1",
                null, null, 0L, 0L, null, List.of(stage));
    }

    @Test
    void throws_exception_when_stage_not_found() {
        when(getCompetitionPersistencePort.competitionIdByStage("s-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.getStage("s-1"))
                .isInstanceOf(StageNotFoundException.class);

        verifyNoInteractions(getObdxFederationsConfigurationsPort);
    }

    @Test
    void throws_exception_when_stage_is_deleted() {
        StageSnapshot deleted = new StageSnapshot("s-1", "Stage A", "comp-1", "user-1",
                1000L, 2000L, 0L, 0L, 9999L, List.of(event()));
        when(getCompetitionPersistencePort.competitionIdByStage("s-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition(deleted));

        assertThatThrownBy(() -> serviceCase.getStage("s-1"))
                .isInstanceOf(StageAlreadyDeletedException.class);

        verifyNoInteractions(getObdxFederationsConfigurationsPort);
    }

    @Test
    void returns_stage_with_empty_events_when_stage_has_no_events() throws IOException {
        StageSnapshot noEvents = new StageSnapshot("s-1", "Stage A", "comp-1", "user-1",
                1000L, 2000L, 0L, 0L, null, List.of());
        when(getCompetitionPersistencePort.competitionIdByStage("s-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition(noEvents));
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenReturn(List.of());

        FetchStageDetailDTO result = serviceCase.getStage("s-1");

        assertThat(result.id()).isEqualTo("s-1");
        assertThat(result.events()).isEmpty();
    }

    @Test
    void returns_stage_with_resolved_configuration_names() throws IOException {
        StageSnapshot stage = new StageSnapshot("s-1", "Stage A", "comp-1", "user-1",
                1000L, 2000L, 0L, 0L, null, List.of(event()));

        ConfigurationDTO config = new ConfigurationDTO("obdx-1", "Obedience", List.of());
        ConfigurationsDTO federation = new ConfigurationsDTO(
                new FederationInfoDTO("FED", "Federation"), List.of(config));

        when(getCompetitionPersistencePort.competitionIdByStage("s-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition(stage));
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenReturn(List.of(federation));

        FetchStageDetailDTO result = serviceCase.getStage("s-1");

        assertThat(result.id()).isEqualTo("s-1");
        assertThat(result.deletedAt()).isNull();
        assertThat(result.status()).isEqualTo("FINISHED");
        assertThat(result.events()).hasSize(1);
        assertThat(result.events().getFirst().configurationName()).isEqualTo("Obedience");
    }

    @Test
    void enrollment_is_closed_when_event_has_no_deadline() throws IOException {
        StageSnapshot stage = new StageSnapshot("s-1", "Stage A", "comp-1", "user-1",
                1000L, 2000L, 0L, 0L, null, List.of(event()));

        ConfigurationDTO config = new ConfigurationDTO("obdx-1", "Obedience", List.of());
        ConfigurationsDTO federation = new ConfigurationsDTO(
                new FederationInfoDTO("FED", "Federation"), List.of(config));

        when(getCompetitionPersistencePort.competitionIdByStage("s-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition(stage));
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenReturn(List.of(federation));

        FetchStageDetailDTO result = serviceCase.getStage("s-1");

        assertThat(result.events().getFirst().enrollmentOpened()).isFalse();
        assertThat(result.events().getFirst().enrollmentDeadline()).isNull();
    }

    @Test
    void throws_when_configurations_cannot_be_loaded() throws IOException {
        StageSnapshot stage = new StageSnapshot("s-1", "Stage A", "comp-1", "user-1",
                1000L, 2000L, 0L, 0L, null, List.of(event()));

        when(getCompetitionPersistencePort.competitionIdByStage("s-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition(stage));
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenThrow(new IOException());

        assertThatThrownBy(() -> serviceCase.getStage("s-1"))
                .isInstanceOf(DisciplineConfigurationMalformedException.class);
    }
}
