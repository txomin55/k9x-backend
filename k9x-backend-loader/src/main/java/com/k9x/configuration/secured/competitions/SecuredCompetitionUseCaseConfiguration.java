package com.k9x.configuration.secured.competitions;

import com.k9x.application.competitions.port.CreateCompetitionPersistencePort;
import com.k9x.application.competitions.use_case.CreateCompetitionServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredCompetitionUseCaseConfiguration {

    @Bean
    public CreateCompetitionServiceCase createCompetitionServiceCase(CreateCompetitionPersistencePort createCompetitionPersistencePort) {
        return new CreateCompetitionServiceCase(createCompetitionPersistencePort);
    }
}
