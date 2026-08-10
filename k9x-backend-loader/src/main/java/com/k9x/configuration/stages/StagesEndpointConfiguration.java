package com.k9x.configuration.stages;

import com.k9x.application.stages.use_case.GetStageListServiceCase;
import com.k9x.application.stages.use_case.GetStageServiceCase;
import com.k9x.infrastructure.in.rest.endpoints.stages.GetStage;
import com.k9x.infrastructure.in.rest.endpoints.stages.GetStages;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StagesEndpointConfiguration {

    @Bean
    public GetStage getStage(GetStageServiceCase getStageServiceCase, ReferenceNameResolver referenceNameResolver) {
        return new GetStage(getStageServiceCase, referenceNameResolver);
    }

    @Bean
    public GetStages getStages(GetStageListServiceCase getStageListServiceCase, ReferenceNameResolver referenceNameResolver) {
        return new GetStages(getStageListServiceCase, referenceNameResolver);
    }
}
