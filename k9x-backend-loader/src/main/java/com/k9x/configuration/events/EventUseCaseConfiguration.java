package com.k9x.configuration.events;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.use_case.GetObdxClassificationServiceCase;
import com.k9x.application.events.obdx.use_case.port.ClassificationCacheManagerPort;
import com.k9x.application.events.use_case.GetEventClassificationServiceCase;
import com.k9x.application.events.use_case.port.EventClassificationCacheManagerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventUseCaseConfiguration {

    @Bean
    public GetObdxClassificationServiceCase getObdxClassificationServiceCase(
            GetObdxClassificationConfigPort getObdxClassificationConfigPort,
            ClassificationCacheManagerPort classificationCacheManagerPort) {
        return new GetObdxClassificationServiceCase(
                getObdxClassificationConfigPort,
                classificationCacheManagerPort);
    }

    @Bean
    public GetEventClassificationServiceCase getClassificationServiceCase(
            GetCompetitionPersistencePort getCompetitionPersistencePort,
            EventClassificationCacheManagerPort eventClassificationCacheManagerPort,
            GetObdxClassificationServiceCase getObdxClassificationServiceCase,
            GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort) {
        return new GetEventClassificationServiceCase(
                getCompetitionPersistencePort,
                eventClassificationCacheManagerPort,
                getObdxClassificationServiceCase,
                getObdxFederationsConfigurationsPort);
    }
}
