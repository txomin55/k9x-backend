package com.k9x.infrastructure.configuration.postgres;

import com.k9x.application.stages.port.CreateStagePersistencePort;
import com.k9x.application.stages.port.DeleteStagePersistencePort;
import com.k9x.application.stages.port.GetStageDetailPersistencePort;
import com.k9x.application.stages.port.GetStageListPersistencePort;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.application.stages.port.UpdateStagePersistencePort;
import com.k9x.infrastructure.out.postgres.stages.CreateStageJooqAdapter;
import com.k9x.infrastructure.out.postgres.stages.DeleteStageJooqAdapter;
import com.k9x.infrastructure.out.postgres.stages.GetStageDetailJooqAdapter;
import com.k9x.infrastructure.out.postgres.stages.GetStageJooqAdapter;
import com.k9x.infrastructure.out.postgres.stages.GetStagesJooqAdapter;
import com.k9x.infrastructure.out.postgres.stages.UpdateStageJooqAdapter;
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
    public CreateStagePersistencePort createStagePersistencePort() {
        return new CreateStageJooqAdapter(dsl);
    }

    @Bean
    public GetStagePersistencePort getStagePersistencePort() {
        return new GetStageJooqAdapter(dsl);
    }

    @Bean
    public UpdateStagePersistencePort updateStagePersistencePort() {
        return new UpdateStageJooqAdapter(dsl);
    }

    @Bean
    public DeleteStagePersistencePort deleteStagePersistencePort() {
        return new DeleteStageJooqAdapter(dsl);
    }

    @Bean
    public GetStageListPersistencePort getStageListPersistencePort() {
        return new GetStagesJooqAdapter(dsl);
    }

    @Bean
    public GetStageDetailPersistencePort getStageDetailPersistencePort() {
        return new GetStageDetailJooqAdapter(dsl);
    }
}
