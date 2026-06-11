package com.k9x.infrastructure.configuration.postgres;

import com.k9x.application.competitions.port.GetCompetitionListPersistencePort;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.infrastructure.out.postgres.competitions.GetCompetitionJooqAdapter;
import com.k9x.infrastructure.out.postgres.competitions.GetCompetitionListJooqAdapter;
import com.k9x.infrastructure.out.postgres.competitions.SaveCompetitionJooqAdapter;
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
    public GetCompetitionPersistencePort getCompetitionPersistencePort() {
        return new GetCompetitionJooqAdapter(dsl);
    }

    @Bean
    public GetCompetitionListPersistencePort getCompetitionListPersistencePort() {
        return new GetCompetitionListJooqAdapter(dsl);
    }

    @Bean
    public SaveCompetitionPersistencePort saveCompetitionPersistencePort() {
        return new SaveCompetitionJooqAdapter(dsl);
    }
}
