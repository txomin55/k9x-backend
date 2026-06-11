package com.k9x.application.stages.use_case;

import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.dto.ConfigurationDTO;
import com.k9x.application.disciplines.use_case.dto.ConfigurationsDTO;
import com.k9x.application.disciplines.use_case.dto.FederationInfoDTO;
import com.k9x.application.stages.port.GetStageListPersistencePort;
import com.k9x.application.stages.use_case.dto.FetchStageListDTO;
import com.k9x.application.stages.use_case.dto.FetchStageListEventDTO;
import com.k9x.domain.disciplines.exceptions.DisciplineConfigurationMalformedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStageListServiceCaseTest {

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
    void resolves_discipline_name_from_configurations() throws IOException {
        FetchStageListEventDTO event = new FetchStageListEventDTO("evt-1", "Open", "obdx-1", null, 3, null);
        FetchStageListDTO stage = new FetchStageListDTO("s-1", "Stage A", "desc", "ES",
                "Calle Mayor 1", 40.4, -3.7, 1000L, 2000L, "Organizer Name", List.of(event), null);

        ConfigurationDTO config = new ConfigurationDTO("obdx-1", "Obedience", List.of());
        ConfigurationsDTO federation = new ConfigurationsDTO(
                new FederationInfoDTO("FED", "Federation", "ES"), List.of(config));

        when(getStageListPersistencePort.getStages()).thenReturn(List.of(stage));
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenReturn(List.of(federation));

        List<FetchStageListDTO> result = serviceCase.getStages();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().events()).hasSize(1);
        assertThat(result.getFirst().events().getFirst().disciplineName()).isEqualTo("Obedience");
        // dateTo = 2000L (1970) is strictly before today's UTC day -> FINISHED.
        assertThat(result.getFirst().events().getFirst().status()).isEqualTo("FINISHED");
        assertThat(result.getFirst().status()).isEqualTo("FINISHED");
    }

    @Test
    void uses_configuration_id_as_fallback_when_discipline_not_found() throws IOException {
        FetchStageListEventDTO event = new FetchStageListEventDTO("evt-1", "Open", "unknown-id", null, 2, null);
        FetchStageListDTO stage = new FetchStageListDTO("s-1", "Stage A", "desc", "ES",
                "Calle Mayor 1", 40.4, -3.7, 1000L, 2000L, "Organizer Name", List.of(event), null);

        when(getStageListPersistencePort.getStages()).thenReturn(List.of(stage));
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenReturn(List.of());

        List<FetchStageListDTO> result = serviceCase.getStages();

        assertThat(result.getFirst().events().getFirst().disciplineName()).isEqualTo("unknown-id");
    }

    @Test
    void throws_when_configurations_cannot_be_loaded() throws IOException {
        when(getObdxFederationsConfigurationsPort.getConfigurations()).thenThrow(new IOException());

        assertThatThrownBy(() -> serviceCase.getStages())
                .isInstanceOf(DisciplineConfigurationMalformedException.class);
    }
}
