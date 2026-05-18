package com.k9x.configuration.secured.competitions;

import com.k9x.application.competitions.port.CreateCompetitionPersistencePort;
import com.k9x.application.competitions.port.DeleteCompetitionPersistencePort;
import com.k9x.application.competitions.port.GeoCoordinatesPort;
import com.k9x.application.competitions.port.GetCompetitionListPersistencePort;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.UpdateCompetitionPersistencePort;
import com.k9x.application.competitions.use_case.CreateCompetitionServiceCase;
import com.k9x.application.competitions.use_case.DeleteCompetitionServiceCase;
import com.k9x.application.competitions.use_case.GetCompetitionListServiceCase;
import com.k9x.application.competitions.use_case.UpdateCompetitionServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredCompetitionUseCaseConfiguration {

    @Bean
    public CreateCompetitionServiceCase createCompetitionServiceCase(CreateCompetitionPersistencePort createCompetitionPersistencePort) {
        return new CreateCompetitionServiceCase(createCompetitionPersistencePort);
    }

    @Bean
    public UpdateCompetitionServiceCase updateCompetitionServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                                     GeoCoordinatesPort geoCoordinatesPort,
                                                                     UpdateCompetitionPersistencePort updateCompetitionPersistencePort) {
        return new UpdateCompetitionServiceCase(getCompetitionPersistencePort, geoCoordinatesPort, updateCompetitionPersistencePort);
    }

    @Bean
    public DeleteCompetitionServiceCase deleteCompetitionServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                                     DeleteCompetitionPersistencePort deleteCompetitionPersistencePort) {
        return new DeleteCompetitionServiceCase(getCompetitionPersistencePort, deleteCompetitionPersistencePort);
    }

    @Bean
    public GetCompetitionListServiceCase getCompetitionListServiceCase(GetCompetitionListPersistencePort getCompetitionListPersistencePort) {
        return new GetCompetitionListServiceCase(getCompetitionListPersistencePort);
    }
}
