package com.k9x.configuration.stages;

import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.stages.port.GetStageListPersistencePort;
import com.k9x.application.stages.use_case.GetStageListServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StageUseCaseConfiguration {

    @Bean
    public GetStageListServiceCase getStageListServiceCase(GetStageListPersistencePort getStageListPersistencePort,
                                                           GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort) {
        return new GetStageListServiceCase(getStageListPersistencePort, getObdxFederationsConfigurationsPort);
    }
}
