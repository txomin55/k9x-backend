package com.k9x.application.disciplines.use_case;

import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.dto.DisciplineConfigurationsDTO;
import com.k9x.domain.disciplines.exceptions.DisciplineConfigurationMalformedException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetDisciplineFederationsConfigurationsServiceCaseTest {

    @Mock
    private GetObdxFederationsConfigurationsPort port;

    private GetDisciplineFederationsConfigurationsServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetDisciplineFederationsConfigurationsServiceCase(port);
    }

    @Test
    void throws_exception_when_not_organizer() {
        assertThatThrownBy(() -> serviceCase.getDisciplineConfigurations("OBDX", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(port);
    }

    @Test
    void returns_configurations_when_organizer() throws IOException {
        when(port.getConfigurations()).thenReturn(List.of());

        DisciplineConfigurationsDTO result = serviceCase.getDisciplineConfigurations("OBDX", true);

        assertThat(result.obdx()).isEmpty();
        verify(port).getConfigurations();
    }

    @Test
    void throws_discipline_configuration_malformed_when_port_fails() throws IOException {
        when(port.getConfigurations()).thenThrow(new IOException("error"));

        assertThatThrownBy(() -> serviceCase.getDisciplineConfigurations("OBDX", true))
                .isInstanceOf(DisciplineConfigurationMalformedException.class);
    }
}