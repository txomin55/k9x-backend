package com.k9x.infrastructure.configuration.postgres;

import com.k9x.application.collections.port.GetCollectionCompetitorsPersistencePort;
import com.k9x.application.collections.port.GetCollectionEventJudgesPersistencePort;
import com.k9x.application.collections.port.GetCollectionExercisesPersistencePort;
import com.k9x.application.collections.port.GetCollectionListPersistencePort;
import com.k9x.application.collections.port.GetCollectionScoresPersistencePort;
import com.k9x.infrastructure.out.postgres.collections.GetCollectionCompetitorsJooqAdapter;
import com.k9x.infrastructure.out.postgres.collections.GetCollectionEventJudgesJooqAdapter;
import com.k9x.infrastructure.out.postgres.collections.GetCollectionExercisesJooqAdapter;
import com.k9x.infrastructure.out.postgres.collections.GetCollectionListJooqAdapter;
import com.k9x.infrastructure.out.postgres.collections.GetCollectionScoresJooqAdapter;
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

    @Bean
    public GetCollectionEventJudgesPersistencePort getCollectionEventJudgesPersistencePort() {
        return new GetCollectionEventJudgesJooqAdapter(dsl);
    }

    @Bean
    public GetCollectionCompetitorsPersistencePort getCollectionCompetitorsPersistencePort() {
        return new GetCollectionCompetitorsJooqAdapter(dsl);
    }

    @Bean
    public GetCollectionExercisesPersistencePort getCollectionExercisesPersistencePort() {
        return new GetCollectionExercisesJooqAdapter(dsl);
    }

    @Bean
    public GetCollectionScoresPersistencePort getCollectionScoresPersistencePort() {
        return new GetCollectionScoresJooqAdapter(dsl);
    }
}
