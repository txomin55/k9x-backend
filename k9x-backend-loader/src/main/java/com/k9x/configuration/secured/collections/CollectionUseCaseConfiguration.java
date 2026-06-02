package com.k9x.configuration.secured.collections;

import com.k9x.application.collections.port.*;
import com.k9x.application.collections.use_case.GetCollectionListServiceCase;
import com.k9x.application.collections.use_case.GetObdxCollectionServiceCase;
import com.k9x.application.disciplines.obdx.port.GetObdxConfigurationAllowedValuesPort;
import com.k9x.application.events.obdx.use_cases.port.GetEventPersistencePort;
import com.k9x.application.stages.port.GetStagePersistencePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CollectionUseCaseConfiguration {

    @Bean
    public GetCollectionListServiceCase getCollectionListServiceCase(GetCollectionListPersistencePort getCollectionListPersistencePort) {
        return new GetCollectionListServiceCase(getCollectionListPersistencePort);
    }

    @Bean
    public GetObdxCollectionServiceCase getCollectionServiceCase(
            GetEventPersistencePort getEventPersistencePort,
            GetStagePersistencePort getStagePersistencePort,
            GetObdxCollectionEventJudgesPersistencePort getObdxCollectionEventJudgesPersistencePort,
            GetObdxCollectionCompetitorsPersistencePort getObdxCollectionCompetitorsPersistencePort,
            GetObdxCollectionExercisesPersistencePort getObdxCollectionExercisesPersistencePort,
            GetObdxCollectionScoresPersistencePort getObdxCollectionScoresPersistencePort,
            GetObdxConfigurationAllowedValuesPort getObdxConfigurationAllowedValuesPort) {
        return new GetObdxCollectionServiceCase(
                getEventPersistencePort,
                getStagePersistencePort,
                getObdxCollectionEventJudgesPersistencePort,
                getObdxCollectionCompetitorsPersistencePort,
                getObdxCollectionExercisesPersistencePort,
                getObdxCollectionScoresPersistencePort,
                getObdxConfigurationAllowedValuesPort);
    }
}
