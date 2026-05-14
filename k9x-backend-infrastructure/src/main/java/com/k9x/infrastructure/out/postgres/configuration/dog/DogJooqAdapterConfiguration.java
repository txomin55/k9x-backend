package com.k9x.infrastructure.out.postgres.configuration.dog;

import com.k9x.application.dog.port.GetDogListPersistencePort;
import com.k9x.application.dog.port.GetDogPersistencePort;
import com.k9x.infrastructure.out.postgres.dog.adapter.GetDogJooqAdapter;
import com.k9x.infrastructure.out.postgres.dog.adapter.GetDogListJooqAdapter;
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

    @Bean
    public GetDogPersistencePort getDogPersistencePort() {
        return new GetDogJooqAdapter(dsl);
    }
}
