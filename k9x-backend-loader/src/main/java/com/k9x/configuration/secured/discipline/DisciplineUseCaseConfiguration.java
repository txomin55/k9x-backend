package com.k9x.configuration.secured.discipline;

import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.GetDisciplineFederationsConfigurationsServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DisciplineUseCaseConfiguration {

    @Bean
    public GetDisciplineFederationsConfigurationsServiceCase getDisciplineFederationsConfigurationsServiceCase(
            GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort) {
        return new GetDisciplineFederationsConfigurationsServiceCase(getObdxFederationsConfigurationsPort);
    }
}
