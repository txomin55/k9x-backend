package com.k9x.configuration.events;

import com.k9x.application.events.obdx.port.ClassificationCacheManagerPort;
import com.k9x.application.events.obdx.port.GetClassificationPersistencePort;
import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.port.GetObdxEventPersistencePort;
import com.k9x.application.events.obdx.use_case.GetObdxEventClassificationServiceCase;
import com.k9x.application.stages.port.GetStagePersistencePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventUseCaseConfiguration {

    @Bean
    public GetObdxEventClassificationServiceCase getClassificationServiceCase(
            GetObdxEventPersistencePort getObdxEventPersistencePort,
            GetStagePersistencePort getStagePersistencePort,
            GetObdxClassificationConfigPort getObdxClassificationConfigPort,
            ClassificationCacheManagerPort classificationCacheManagerPort,
            GetClassificationPersistencePort getClassificationPersistencePort) {
        return new GetObdxEventClassificationServiceCase(
                getObdxEventPersistencePort,
                getStagePersistencePort,
                getObdxClassificationConfigPort,
                classificationCacheManagerPort,
                getClassificationPersistencePort);
    }
}
