package com.k9x.application.stages.use_case;

import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.obdx.use_case.dto.ObdxConfigurationDTO;
import com.k9x.application.disciplines.obdx.use_case.dto.ObdxConfigurationsDTO;
import com.k9x.application.disciplines.obdx.use_case.dto.ObdxFederationInfoDTO;
import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.application.stages.exceptions.StageHasNoEventsException;
import com.k9x.application.stages.exceptions.StageNotFoundException;
import com.k9x.application.stages.port.GetStageDetailPersistencePort;
import com.k9x.application.stages.use_case.dto.FetchStageDetailDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailEventDTO;
import com.k9x.domain.exceptions.DisciplineConfigurationMalformedException;
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
    private GetStageDetailPersistencePort getStageDetailPersistencePort;

    @Mock
    private GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort;

    private GetStageServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetStageServiceCase(getStageDetailPersistencePort, getObdxFederationsConfigurationsPort);
    }

    @Test
    void throws_exception_when_stage_not_found() {
        when(getStageDetailPersistencePort.getStage("s-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.getStage("s-1"))
                .isInstanceOf(StageNotFoundException.class);

        verifyNoInteractions(getObdxFederationsConfigurationsPort);
    }

    @Test
    void throws_exception_when_stage_is_deleted() {
        FetchStageDetailDTO deleted = new FetchStageDetailDTO("s-1", "Stage A", 1000L, 2000L,
                "Calle Mayor 1", "Organizer", 9999L, List.of());
        when(getStageDetailPersistencePort.getStage("s-1")).thenReturn(deleted);

        assertThatThrownBy(() -> serviceCase.getStage("s-1"))
                .isInstanceOf(StageAlreadyDeletedException.class);

        verifyNoInteractions(getObdxFederationsConfigurationsPort);
    }

    @Test
    void throws_exception_when_stage_has_no_events() {
        FetchStageDetailDTO noEvents = new FetchStageDetailDTO("s-1", "Stage A", 1000L, 2000L,
                "Calle Mayor 1", "Organizer", null, List.of());
        when(getStageDetailPersistencePort.getStage("s-1")).thenReturn(noEvents);

        assertThatThrownBy(() -> serviceCase.getStage("s-1"))
                .isInstanceOf(StageHasNoEventsException.class);

        verifyNoInteractions(getObdxFederationsConfigurationsPort);
    }

    @Test
    void returns_stage_with_resolved_discipline_names() throws IOException {
        FetchStageDetailEventDTO event = new FetchStageDetailEventDTO("evt-1", "Open", "obdx-1", null);
        FetchStageDetailDTO stage = new FetchStageDetailDTO("s-1", "Stage A", 1000L, 2000L,
                "Calle Mayor 1", "Organizer", null, List.of(event));

        ObdxConfigurationDTO config = new ObdxConfigurationDTO("obdx-1", "Obedience", List.of());
        ObdxConfigurationsDTO federation = new ObdxConfigurationsDTO(
                new ObdxFederationInfoDTO("FED", "Federation", "ES"), List.of(config));

        when(getStageDetailPersistencePort.getStage("s-1")).thenReturn(stage);
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenReturn(List.of(federation));

        FetchStageDetailDTO result = serviceCase.getStage("s-1");

        assertThat(result.id()).isEqualTo("s-1");
        assertThat(result.deletedAt()).isNull();
        assertThat(result.events()).hasSize(1);
        assertThat(result.events().getFirst().disciplineName()).isEqualTo("Obedience");
    }

    @Test
    void throws_when_configurations_cannot_be_loaded() throws IOException {
        FetchStageDetailEventDTO event = new FetchStageDetailEventDTO("evt-1", "Open", "obdx-1", null);
        FetchStageDetailDTO stage = new FetchStageDetailDTO("s-1", "Stage A", 1000L, 2000L,
                "Calle Mayor 1", "Organizer", null, List.of(event));

        when(getStageDetailPersistencePort.getStage("s-1")).thenReturn(stage);
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenThrow(new IOException());

        assertThatThrownBy(() -> serviceCase.getStage("s-1"))
                .isInstanceOf(DisciplineConfigurationMalformedException.class);
    }
}
