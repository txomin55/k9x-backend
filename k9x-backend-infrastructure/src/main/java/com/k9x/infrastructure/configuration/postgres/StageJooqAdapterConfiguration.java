package com.k9x.infrastructure.configuration.postgres;

import com.k9x.application.stages.port.GetStageListPersistencePort;
import com.k9x.infrastructure.out.postgres.stages.GetStagesJooqAdapter;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StageJooqAdapterConfiguration {

    private final DSLContext dsl;

    StageJooqAdapterConfiguration(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Bean
    public GetStageListPersistencePort getStageListPersistencePort() {
        return new GetStagesJooqAdapter(dsl);
    }
}
