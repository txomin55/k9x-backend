package com.k9x.infrastructure.configuration.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.dogs.port.CreateDogPersistencePort;
import com.k9x.application.dogs.port.DeleteDogPersistencePort;
import com.k9x.application.dogs.port.GetDogListPersistencePort;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.dogs.port.UpdateDogPersistencePort;
import com.k9x.application.dogs.rank.port.CreateDogRankHistoryPersistencePort;
import com.k9x.application.dogs.rank.port.GetDogRankEventResultsPersistencePort;
import com.k9x.application.dogs.rank.port.GetLatestDogRankHistoryPersistencePort;
import com.k9x.infrastructure.out.postgres.dogs.CreateDogJooqAdapter;
import com.k9x.infrastructure.out.postgres.dogs.CreateDogRankHistoryJooqAdapter;
import com.k9x.infrastructure.out.postgres.dogs.DeleteDogJooqAdapter;
import com.k9x.infrastructure.out.postgres.dogs.GetDogJooqAdapter;
import com.k9x.infrastructure.out.postgres.dogs.GetDogListJooqAdapter;
import com.k9x.infrastructure.out.postgres.dogs.GetDogRankEventResultsJooqAdapter;
import com.k9x.infrastructure.out.postgres.dogs.GetLatestDogRankHistoryJooqAdapter;
import com.k9x.infrastructure.out.postgres.dogs.UpdateDogJooqAdapter;
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
    public CreateDogPersistencePort createDogPersistencePort() {
        return new CreateDogJooqAdapter(dsl);
    }

    @Bean
    public GetDogPersistencePort getDogPersistencePort() {
        return new GetDogJooqAdapter(dsl);
    }

    @Bean
    public GetDogListPersistencePort getDogListPersistencePort() {
        return new GetDogListJooqAdapter(dsl);
    }

    @Bean
    public DeleteDogPersistencePort deleteDogPersistencePort() {
        return new DeleteDogJooqAdapter(dsl);
    }

    @Bean
    public UpdateDogPersistencePort updateDogPersistencePort() {
        return new UpdateDogJooqAdapter(dsl);
    }

    @Bean
    public GetDogRankEventResultsPersistencePort getDogRankEventResultsPersistencePort() {
        return new GetDogRankEventResultsJooqAdapter(dsl);
    }

    @Bean
    public GetLatestDogRankHistoryPersistencePort getLatestDogRankHistoryPersistencePort() {
        return new GetLatestDogRankHistoryJooqAdapter(dsl);
    }

    @Bean
    public CreateDogRankHistoryPersistencePort createDogRankHistoryPersistencePort(ObjectMapper objectMapper) {
        return new CreateDogRankHistoryJooqAdapter(dsl, objectMapper);
    }
}
