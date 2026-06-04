package com.k9x.configuration.events;

import com.k9x.application.events.use_case.GetEventClassificationServiceCase;
import com.k9x.infrastructure.in.rest.endpoints.events.GetEventClassification;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventsEndpointConfiguration {

    @Bean
    public GetEventClassification getEventClassification(GetEventClassificationServiceCase getClassificationServiceCase,
                                                         MessageSource messageSource) {
        return new GetEventClassification(getClassificationServiceCase, messageSource);
    }
}
