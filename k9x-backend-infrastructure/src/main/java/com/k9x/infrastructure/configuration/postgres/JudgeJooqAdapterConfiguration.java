package com.k9x.infrastructure.configuration.postgres;

import com.k9x.application.judges.port.CreateJudgePersistencePort;
import com.k9x.application.judges.port.DeleteJudgePersistencePort;
import com.k9x.application.judges.port.GetJudgeListPersistencePort;
import com.k9x.application.judges.port.GetJudgePersistencePort;
import com.k9x.application.judges.port.UpdateJudgePersistencePort;
import com.k9x.infrastructure.out.postgres.judges.CreateJudgeJooqAdapter;
import com.k9x.infrastructure.out.postgres.judges.DeleteJudgeJooqAdapter;
import com.k9x.infrastructure.out.postgres.judges.GetJudgeJooqAdapter;
import com.k9x.infrastructure.out.postgres.judges.GetJudgeListJooqAdapter;
import com.k9x.infrastructure.out.postgres.judges.UpdateJudgeJooqAdapter;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JudgeJooqAdapterConfiguration {

    private final DSLContext dsl;

    JudgeJooqAdapterConfiguration(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Bean
    public CreateJudgePersistencePort createJudgePersistencePort() {
        return new CreateJudgeJooqAdapter(dsl);
    }

    @Bean
    public GetJudgeListPersistencePort getJudgeListPersistencePort() {
        return new GetJudgeListJooqAdapter(dsl);
    }

    @Bean
    public GetJudgePersistencePort getJudgePersistencePort() {
        return new GetJudgeJooqAdapter(dsl);
    }

    @Bean
    public DeleteJudgePersistencePort deleteJudgePersistencePort() {
        return new DeleteJudgeJooqAdapter(dsl);
    }

    @Bean
    public UpdateJudgePersistencePort updateJudgePersistencePort() {
        return new UpdateJudgeJooqAdapter(dsl);
    }
}
