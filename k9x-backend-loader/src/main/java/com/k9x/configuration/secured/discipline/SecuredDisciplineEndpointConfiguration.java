package com.k9x.configuration.secured.discipline;

import com.k9x.infrastructure.in.rest.endpoints.secured.discipline.GetDisciplines;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredDisciplineEndpointConfiguration {

    @Bean
    public GetDisciplines getDisciplines() {
        return new GetDisciplines();
    }
}
