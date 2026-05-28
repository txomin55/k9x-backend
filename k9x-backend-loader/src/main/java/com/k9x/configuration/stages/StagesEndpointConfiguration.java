package com.k9x.configuration.stages;

import com.k9x.application.stages.use_case.GetStageListServiceCase;
import com.k9x.infrastructure.in.rest.endpoints.stages.GetStage;
import com.k9x.infrastructure.in.rest.endpoints.stages.GetStages;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StagesEndpointConfiguration {

    @Bean
    public GetStage getStage() {
        return new GetStage();
    }

    @Bean
    public GetStages getStages(GetStageListServiceCase getStageListServiceCase) {
        return new GetStages(getStageListServiceCase);
    }
}
