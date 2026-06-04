package com.k9x.configuration.secured.collections;

import com.k9x.application.collections.obdx.port.GetObdxCollectionCompetitorsPersistencePort;
import com.k9x.application.collections.obdx.port.GetObdxCollectionEventJudgesPersistencePort;
import com.k9x.application.collections.obdx.port.GetObdxCollectionExercisesPersistencePort;
import com.k9x.application.collections.obdx.port.GetObdxCollectionScoresPersistencePort;
import com.k9x.application.collections.obdx.use_case.GetObdxCollectionServiceCase;
import com.k9x.application.collections.port.GetCollectionListPersistencePort;
import com.k9x.application.collections.use_case.GetCollectionListServiceCase;
import com.k9x.application.collections.use_case.GetCollectionServiceCase;
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
    public GetObdxCollectionServiceCase getObdxCollectionServiceCase(
            GetObdxCollectionCompetitorsPersistencePort getObdxCollectionCompetitorsPersistencePort,
            GetObdxCollectionExercisesPersistencePort getObdxCollectionExercisesPersistencePort,
            GetObdxCollectionScoresPersistencePort getObdxCollectionScoresPersistencePort) {
        return new GetObdxCollectionServiceCase(
                getObdxCollectionCompetitorsPersistencePort,
                getObdxCollectionExercisesPersistencePort,
                getObdxCollectionScoresPersistencePort);
    }

    @Bean
    public GetCollectionServiceCase getCollectionServiceCase(
            GetEventPersistencePort getEventPersistencePort,
            GetStagePersistencePort getStagePersistencePort,
            GetObdxCollectionEventJudgesPersistencePort getObdxCollectionEventJudgesPersistencePort,
            GetObdxConfigurationAllowedValuesPort getObdxConfigurationAllowedValuesPort,
            GetObdxCollectionServiceCase getObdxCollectionServiceCase) {
        return new GetCollectionServiceCase(
                getEventPersistencePort,
                getStagePersistencePort,
                getObdxCollectionEventJudgesPersistencePort,
                getObdxConfigurationAllowedValuesPort,
                getObdxCollectionServiceCase);
    }
}
