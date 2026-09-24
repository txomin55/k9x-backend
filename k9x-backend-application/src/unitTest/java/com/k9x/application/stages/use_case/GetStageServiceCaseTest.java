package com.k9x.application.stages.use_case;

import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.notifications.port.GetStageNotificationsPersistencePort;
import com.k9x.application.disciplines.use_case.dto.ConfigurationDTO;
import com.k9x.application.disciplines.use_case.dto.ConfigurationsDTO;
import com.k9x.application.disciplines.use_case.dto.FederationInfoDTO;
import com.k9x.application.stages.port.GetStageDetailPersistencePort;
import com.k9x.application.stages.use_case.dto.FetchStageDetailCompetitorDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailEventDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailRowDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailRowEventDTO;
import com.k9x.domain.disciplines.exceptions.DisciplineConfigurationMalformedException;
import com.k9x.domain.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.domain.stages.exceptions.StageNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStageServiceCaseTest {

    private static final long FAR_FUTURE = 4_000_000_000_000L; // year 2096

    @Mock
    private GetStageDetailPersistencePort getStageDetailPersistencePort;

    @Mock
    private GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort;

    @Mock
    private GetStageNotificationsPersistencePort getStageNotificationsPersistencePort;

    private GetStageServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetStageServiceCase(getStageDetailPersistencePort, getObdxFederationsConfigurationsPort,
                getStageNotificationsPersistencePort);
    }

    private static FetchStageDetailRowEventDTO event(String id, Long deletedAt, boolean hasAnyScore, boolean settled,
                                                     List<FetchStageDetailCompetitorDTO> competitors) {
        return new FetchStageDetailRowEventDTO(id, "Open", "obdx", "obdx-1", deletedAt, null, List.of(), null,
                competitors, hasAnyScore, settled);
    }

    private static FetchStageDetailRowDTO stage(long from, long to, Long deletedAt, List<FetchStageDetailRowEventDTO> events) {
        return new FetchStageDetailRowDTO("s-1", "Stage A", from, to, deletedAt, "World Cup", "Calle Mayor 1",
                "Organizer", null, events);
    }

    private void stageIs(FetchStageDetailRowDTO stage) {
        when(getStageDetailPersistencePort.getStage(eq("s-1"), anyLong())).thenReturn(Optional.of(stage));
    }

    private void configurationsAre(ConfigurationDTO... configurations) throws IOException {
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenReturn(List.of(
                new ConfigurationsDTO(new FederationInfoDTO("FED", "Federation"), List.of(configurations))));
    }

    @Test
    void throws_exception_when_stage_not_found() {
        when(getStageDetailPersistencePort.getStage(eq("s-1"), anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceCase.getStage("s-1"))
                .isInstanceOf(StageNotFoundException.class);

        verifyNoInteractions(getObdxFederationsConfigurationsPort);
    }

    @Test
    void throws_exception_when_stage_is_deleted() {
        stageIs(stage(1000L, 2000L, 9999L, List.of(event("evt-1", null, false, false, List.of()))));

        assertThatThrownBy(() -> serviceCase.getStage("s-1"))
                .isInstanceOf(StageAlreadyDeletedException.class);

        verifyNoInteractions(getObdxFederationsConfigurationsPort);
    }

    @Test
    void returns_stage_with_empty_events_when_stage_has_no_events() throws IOException {
        stageIs(stage(1000L, 2000L, null, List.of()));
        configurationsAre();

        FetchStageDetailDTO result = serviceCase.getStage("s-1");

        assertThat(result.id()).isEqualTo("s-1");
        assertThat(result.events()).isEmpty();
    }

    @Test
    void returns_stage_with_resolved_configuration_names_and_competitors() throws IOException {
        FetchStageDetailCompetitorDTO competitor = new FetchStageDetailCompetitorDTO("dog-1", "Rex", "Owner",
                "Handler", "ES", "Team", "Border Collie", true);
        stageIs(stage(1000L, 2000L, null, List.of(event("evt-1", null, false, false, List.of(competitor)))));
        configurationsAre(new ConfigurationDTO("obdx-1", "Obedience", List.of()));

        FetchStageDetailDTO result = serviceCase.getStage("s-1");

        assertThat(result.id()).isEqualTo("s-1");
        assertThat(result.competitionName()).isEqualTo("World Cup");
        assertThat(result.deletedAt()).isNull();
        assertThat(result.status()).isEqualTo("FINISHED");
        assertThat(result.events()).hasSize(1);
        assertThat(result.events().getFirst().configurationName()).isEqualTo("Obedience");
        assertThat(result.events().getFirst().competitors()).containsExactly(competitor);
    }

    @Test
    void resolves_live_statuses_from_the_projected_facts_and_hides_deleted_events() throws IOException {
        stageIs(stage(1000L, FAR_FUTURE, null, List.of(
                event("evt-started", null, true, false, List.of()),
                event("evt-settled", null, true, true, List.of()),
                event("evt-deleted", 1L, false, false, List.of()))));
        configurationsAre();

        FetchStageDetailDTO result = serviceCase.getStage("s-1");

        assertThat(result.status()).isEqualTo("STARTED");
        assertThat(result.events()).extracting(FetchStageDetailEventDTO::id, FetchStageDetailEventDTO::status)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("evt-started", "STARTED"),
                        org.assertj.core.groups.Tuple.tuple("evt-settled", "FINISHED"));
    }

    @Test
    void enrollment_is_closed_when_event_has_no_deadline() throws IOException {
        stageIs(stage(1000L, 2000L, null, List.of(event("evt-1", null, false, false, List.of()))));
        configurationsAre(new ConfigurationDTO("obdx-1", "Obedience", List.of()));

        FetchStageDetailDTO result = serviceCase.getStage("s-1");

        assertThat(result.events().getFirst().enrollmentOpened()).isFalse();
        assertThat(result.events().getFirst().enrollmentDeadline()).isNull();
    }

    @Test
    void throws_when_configurations_cannot_be_loaded() throws IOException {
        stageIs(stage(1000L, 2000L, null, List.of(event("evt-1", null, false, false, List.of()))));
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenThrow(new IOException());

        assertThatThrownBy(() -> serviceCase.getStage("s-1"))
                .isInstanceOf(DisciplineConfigurationMalformedException.class);
    }
}
