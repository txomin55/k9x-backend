package com.k9x.infrastructure.configuration.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.dogs.port.CreateDogPersistencePort;
import com.k9x.application.dogs.port.DeleteDogPersistencePort;
import com.k9x.application.dogs.port.GetDogListPersistencePort;
import com.k9x.application.dogs.port.GetDogParticipationsPersistencePort;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.dogs.port.GetPublicDogListPersistencePort;
import com.k9x.application.dogs.port.UpdateDogPersistencePort;
import com.k9x.application.dogs.rank.port.CreateDogRankHistoryPersistencePort;
import com.k9x.application.dogs.rank.port.GetDogIndexEventsPersistencePort;
import com.k9x.application.dogs.rank.port.GetDogRankEventResultsPersistencePort;
import com.k9x.application.dogs.rank.port.GetDogRankingDistributionPersistencePort;
import com.k9x.application.dogs.rank.port.GetDogRankingEntryPersistencePort;
import com.k9x.application.dogs.rank.port.GetLatestDogRankHistoryPersistencePort;
import com.k9x.application.dogs.rank.port.ReplaceDogRankingSnapshotPersistencePort;
import com.k9x.infrastructure.out.postgres.dogs.CreateDogJooqAdapter;
import com.k9x.infrastructure.out.postgres.dogs.CreateDogRankHistoryJooqAdapter;
import com.k9x.infrastructure.out.postgres.dogs.DeleteDogJooqAdapter;
import com.k9x.infrastructure.out.postgres.dogs.GetDogIndexEventsJooqAdapter;
import com.k9x.infrastructure.out.postgres.dogs.GetDogJooqAdapter;
import com.k9x.infrastructure.out.postgres.dogs.GetDogListJooqAdapter;
import com.k9x.infrastructure.out.postgres.dogs.GetDogParticipationsJooqAdapter;
import com.k9x.infrastructure.out.postgres.dogs.GetDogRankEventResultsJooqAdapter;
import com.k9x.infrastructure.out.postgres.dogs.GetDogRankingDistributionJooqAdapter;
import com.k9x.infrastructure.out.postgres.dogs.GetDogRankingEntryJooqAdapter;
import com.k9x.infrastructure.out.postgres.dogs.GetLatestDogRankHistoryJooqAdapter;
import com.k9x.infrastructure.out.postgres.dogs.GetPublicDogListJooqAdapter;
import com.k9x.infrastructure.out.postgres.dogs.ReplaceDogRankingSnapshotJooqAdapter;
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
    public GetPublicDogListPersistencePort getPublicDogListPersistencePort() {
        return new GetPublicDogListJooqAdapter(dsl);
    }

    @Bean
    public GetDogParticipationsPersistencePort getDogParticipationsPersistencePort() {
        return new GetDogParticipationsJooqAdapter(dsl);
    }

    @Bean
    public GetDogIndexEventsPersistencePort getDogIndexEventsPersistencePort() {
        return new GetDogIndexEventsJooqAdapter(dsl);
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

    @Bean
    public ReplaceDogRankingSnapshotPersistencePort replaceDogRankingSnapshotPersistencePort() {
        return new ReplaceDogRankingSnapshotJooqAdapter(dsl);
    }

    @Bean
    public GetDogRankingDistributionPersistencePort getDogRankingDistributionPersistencePort() {
        return new GetDogRankingDistributionJooqAdapter(dsl);
    }

    @Bean
    public GetDogRankingEntryPersistencePort getDogRankingEntryPersistencePort() {
        return new GetDogRankingEntryJooqAdapter(dsl);
    }
}
