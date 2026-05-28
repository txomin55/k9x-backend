package com.k9x.infrastructure.configuration.postgres;

import com.k9x.application.collections.port.GetCollectionListPersistencePort;
import com.k9x.infrastructure.out.postgres.collections.GetCollectionListJooqAdapter;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CollectionJooqAdapterConfiguration {

    private final DSLContext dsl;

    CollectionJooqAdapterConfiguration(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Bean
    public GetCollectionListPersistencePort getCollectionListPersistencePort() {
        return new GetCollectionListJooqAdapter(dsl);
    }
}
