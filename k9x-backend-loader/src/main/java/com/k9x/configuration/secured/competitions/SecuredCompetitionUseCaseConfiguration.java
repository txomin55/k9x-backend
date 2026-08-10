package com.k9x.configuration.secured.competitions;

import com.k9x.application.competitions.port.GeoCoordinatesPort;
import com.k9x.application.competitions.port.GetCompetitionListPersistencePort;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.competitions.use_case.CreateCompetitionServiceCase;
import com.k9x.application.competitions.use_case.DeleteCompetitionServiceCase;
import com.k9x.application.competitions.use_case.GetCompetitionListServiceCase;
import com.k9x.application.competitions.use_case.UpdateCompetitionServiceCase;
import com.k9x.application.notifications.port.GetStageNotificationsPersistencePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.k9x.application.competitions.port.GetSelectableCompetitionsPersistencePort;
import com.k9x.application.competitions.use_case.GetSelectableCompetitionListServiceCase;

@Configuration
public class SecuredCompetitionUseCaseConfiguration {

    @Bean
    public CreateCompetitionServiceCase createCompetitionServiceCase(SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        return new CreateCompetitionServiceCase(saveCompetitionPersistencePort);
    }

    @Bean
    public UpdateCompetitionServiceCase updateCompetitionServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                                     GeoCoordinatesPort geoCoordinatesPort,
                                                                     SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        return new UpdateCompetitionServiceCase(getCompetitionPersistencePort, geoCoordinatesPort, saveCompetitionPersistencePort);
    }

    @Bean
    public DeleteCompetitionServiceCase deleteCompetitionServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                                     SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        return new DeleteCompetitionServiceCase(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Bean
    public GetCompetitionListServiceCase getCompetitionListServiceCase(
            GetCompetitionListPersistencePort getCompetitionListPersistencePort,
            GetStageNotificationsPersistencePort getStageNotificationsPersistencePort) {
        return new GetCompetitionListServiceCase(getCompetitionListPersistencePort,
                getStageNotificationsPersistencePort);
    }

    @Bean
    public GetSelectableCompetitionListServiceCase getSelectableCompetitionListServiceCase(
            GetSelectableCompetitionsPersistencePort getSelectableCompetitionsPersistencePort) {
        return new GetSelectableCompetitionListServiceCase(getSelectableCompetitionsPersistencePort);
    }
}
