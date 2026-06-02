package com.k9x.configuration.events;

import com.k9x.application.events.obdx.port.GetClassificationPersistencePort;
import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.use_cases.port.ClassificationCacheManagerPort;
import com.k9x.application.events.obdx.use_cases.port.GetEventPersistencePort;
import com.k9x.application.events.use_cases.GetEventClassificationServiceCase;
import com.k9x.application.stages.port.GetStagePersistencePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventUseCaseConfiguration {

    @Bean
    public GetEventClassificationServiceCase getClassificationServiceCase(
            GetEventPersistencePort getEventPersistencePort,
            GetStagePersistencePort getStagePersistencePort,
            GetObdxClassificationConfigPort getObdxClassificationConfigPort,
            ClassificationCacheManagerPort classificationCacheManagerPort,
            GetClassificationPersistencePort getClassificationPersistencePort) {
        return new GetEventClassificationServiceCase(
                getEventPersistencePort,
                getStagePersistencePort,
                getObdxClassificationConfigPort,
                classificationCacheManagerPort,
                getClassificationPersistencePort);
    }
}
