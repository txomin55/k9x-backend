package com.k9x.infrastructure.configuration.postgres;

import com.k9x.application.extractions.port.GetExtractedCompetitionsPersistencePort;
import com.k9x.infrastructure.out.postgres.extractions.GetExtractedCompetitionsJooqAdapter;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExtractionJooqAdapterConfiguration {

    private final DSLContext dsl;

    ExtractionJooqAdapterConfiguration(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Bean
    public GetExtractedCompetitionsPersistencePort getExtractedCompetitionsPersistencePort() {
        return new GetExtractedCompetitionsJooqAdapter(dsl);
    }
}
