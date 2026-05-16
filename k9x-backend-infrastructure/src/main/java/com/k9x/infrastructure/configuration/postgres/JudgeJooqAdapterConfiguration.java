package com.k9x.infrastructure.configuration.postgres;

import com.k9x.application.judges.port.GetJudgeListPersistencePort;
import com.k9x.infrastructure.out.postgres.judges.GetJudgeListJooqAdapter;
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
    public GetJudgeListPersistencePort getJudgeListPersistencePort() {
        return new GetJudgeListJooqAdapter(dsl);
    }
}
