package com.k9x.infrastructure.in.rest.configuration.session.config;

import com.k9x.application.events.obdx.port.ClassificationCacheManagerPort;
import com.k9x.infrastructure.out.cache.ClassificationCacheManagerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClassificationCacheConfiguration {

    @Bean
    public ClassificationCacheManagerPort classificationCacheManagerPort() {
        return new ClassificationCacheManagerAdapter();
    }
}
