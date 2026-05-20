package com.k9x.configuration.secured.discipline;

import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.obdx.use_case.GetObdxFederationsConfigurationsServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DisciplineUseCaseConfiguration {

    @Bean
    public GetObdxFederationsConfigurationsServiceCase getDisciplineFederationsConfigurationsServiceCase(
            GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort) {
        return new GetObdxFederationsConfigurationsServiceCase(getObdxFederationsConfigurationsPort);
    }
}
