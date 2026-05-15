package com.k9x.configuration.secured.stages;

import com.k9x.infrastructure.in.rest.endpoints.secured.stages.CreateStage;
import com.k9x.infrastructure.in.rest.endpoints.secured.stages.RemoveStage;
import com.k9x.infrastructure.in.rest.endpoints.secured.stages.UpdateStage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredStagesEndpointConfiguration {

    @Bean
    public CreateStage createStage() {
        return new CreateStage();
    }

    @Bean
    public RemoveStage removeStage() {
        return new RemoveStage();
    }

    @Bean
    public UpdateStage updateStage() {
        return new UpdateStage();
    }
}
