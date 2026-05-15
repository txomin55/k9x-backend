package com.k9x.infrastructure.configuration.postgres;

import com.k9x.application.dogs.port.GetDogListPersistencePort;
import com.k9x.infrastructure.out.postgres.dogs.GetDogListJooqAdapter;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DogJooqAdapterConfiguration {

    private final DSLContext dsl;

    DogJooqAdapterConfiguration(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Bean
    public GetDogListPersistencePort getDogListPersistencePort() {
        return new GetDogListJooqAdapter(dsl);
    }
}