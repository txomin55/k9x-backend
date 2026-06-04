package com.k9x.infrastructure.in.rest.configuration.session.config;

import com.k9x.application.events.obdx.use_case.port.ClassificationCacheManagerPort;
import com.k9x.application.events.use_case.port.EventClassificationCacheManagerPort;
import com.k9x.infrastructure.out.cache.ClassificationCacheManagerAdapter;
import com.k9x.infrastructure.out.cache.EventClassificationCacheManagerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClassificationCacheConfiguration {

    @Bean
    public ClassificationCacheManagerPort classificationCacheManagerPort() {
        return new ClassificationCacheManagerAdapter();
    }

    @Bean
    public EventClassificationCacheManagerPort eventClassificationCacheManagerPort() {
        return new EventClassificationCacheManagerAdapter();
    }
}
