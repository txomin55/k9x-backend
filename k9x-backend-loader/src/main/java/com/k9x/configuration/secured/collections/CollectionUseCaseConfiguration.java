package com.k9x.configuration.secured.collections;

import com.k9x.application.collections.port.GetCollectionCompetitorsPersistencePort;
import com.k9x.application.collections.port.GetCollectionEventJudgesPersistencePort;
import com.k9x.application.collections.port.GetCollectionExercisesPersistencePort;
import com.k9x.application.collections.port.GetCollectionListPersistencePort;
import com.k9x.application.collections.port.GetCollectionScoresPersistencePort;
import com.k9x.application.collections.use_case.GetCollectionListServiceCase;
import com.k9x.application.collections.use_case.GetCollectionServiceCase;
import com.k9x.application.disciplines.obdx.port.GetObdxConfigurationAllowedValuesPort;
import com.k9x.application.events.obdx.port.GetObdxEventPersistencePort;
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
    public GetCollectionServiceCase getCollectionServiceCase(
            GetObdxEventPersistencePort getObdxEventPersistencePort,
            GetStagePersistencePort getStagePersistencePort,
            GetCollectionEventJudgesPersistencePort getCollectionEventJudgesPersistencePort,
            GetCollectionCompetitorsPersistencePort getCollectionCompetitorsPersistencePort,
            GetCollectionExercisesPersistencePort getCollectionExercisesPersistencePort,
            GetCollectionScoresPersistencePort getCollectionScoresPersistencePort,
            GetObdxConfigurationAllowedValuesPort getObdxConfigurationAllowedValuesPort) {
        return new GetCollectionServiceCase(
                getObdxEventPersistencePort,
                getStagePersistencePort,
                getCollectionEventJudgesPersistencePort,
                getCollectionCompetitorsPersistencePort,
                getCollectionExercisesPersistencePort,
                getCollectionScoresPersistencePort,
                getObdxConfigurationAllowedValuesPort);
    }
}
