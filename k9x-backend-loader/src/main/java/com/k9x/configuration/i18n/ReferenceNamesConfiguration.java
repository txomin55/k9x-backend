package com.k9x.configuration.i18n;

import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReferenceNamesConfiguration {

    @Bean
    public ReferenceNameResolver referenceNameResolver(MessageSource messageSource) {
        return new ReferenceNameResolver(messageSource);
    }
}
