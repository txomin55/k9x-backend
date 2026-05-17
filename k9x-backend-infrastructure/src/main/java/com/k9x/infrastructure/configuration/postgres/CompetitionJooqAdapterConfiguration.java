package com.k9x.infrastructure.configuration.postgres;

import com.k9x.application.competitions.port.CreateCompetitionPersistencePort;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.UpdateCompetitionPersistencePort;
import com.k9x.infrastructure.out.postgres.competitions.CreateCompetitionJooqAdapter;
import com.k9x.infrastructure.out.postgres.competitions.GetCompetitionJooqAdapter;
import com.k9x.infrastructure.out.postgres.competitions.UpdateCompetitionJooqAdapter;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CompetitionJooqAdapterConfiguration {

    private final DSLContext dsl;

    CompetitionJooqAdapterConfiguration(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Bean
    public CreateCompetitionPersistencePort createCompetitionPersistencePort() {
        return new CreateCompetitionJooqAdapter(dsl);
    }

    @Bean
    public GetCompetitionPersistencePort getCompetitionPersistencePort() {
        return new GetCompetitionJooqAdapter(dsl);
    }

    @Bean
    public UpdateCompetitionPersistencePort updateCompetitionPersistencePort() {
        return new UpdateCompetitionJooqAdapter(dsl);
    }
}
