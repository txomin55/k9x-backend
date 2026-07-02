package com.k9x.configuration.stages;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.stages.port.GetStageListPersistencePort;
import com.k9x.application.stages.use_case.GetStageListServiceCase;
import com.k9x.application.stages.use_case.GetStageServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StageUseCaseConfiguration {

    @Bean
    public GetStageListServiceCase getStageListServiceCase(GetStageListPersistencePort getStageListPersistencePort) {
        return new GetStageListServiceCase(getStageListPersistencePort);
    }

    @Bean
    public GetStageServiceCase getStageServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                   GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort) {
        return new GetStageServiceCase(getCompetitionPersistencePort, getObdxFederationsConfigurationsPort);
    }
}
