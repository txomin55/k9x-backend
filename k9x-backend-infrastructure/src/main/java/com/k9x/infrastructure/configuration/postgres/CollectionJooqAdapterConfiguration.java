package com.k9x.infrastructure.configuration.postgres;

import com.k9x.application.collections.port.*;
import com.k9x.infrastructure.out.postgres.collections.*;
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
    public GetObdxCollectionEventJudgesPersistencePort getCollectionEventJudgesPersistencePort() {
        return new GetObdxCollectionEventJudgesJooqAdapter(dsl);
    }

    @Bean
    public GetObdxCollectionCompetitorsPersistencePort getCollectionCompetitorsPersistencePort() {
        return new GetObdxCollectionCompetitorsJooqAdapter(dsl);
    }

    @Bean
    public GetObdxCollectionExercisesPersistencePort getCollectionExercisesPersistencePort() {
        return new GetObdxCollectionExercisesJooqAdapter(dsl);
    }

    @Bean
    public GetObdxCollectionScoresPersistencePort getCollectionScoresPersistencePort() {
        return new GetObdxCollectionScoresJooqAdapter(dsl);
    }
}
