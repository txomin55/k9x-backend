package com.k9x.configuration.secured.event;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.disciplines.obdx.port.GetObdxExerciseAllowedValuesPort;
import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.events.obdx.port.*;
import com.k9x.application.events.obdx.use_case.UpdateObdxEventServiceCase;
import com.k9x.application.events.obdx.use_case.UpdateObdxScoreServiceCase;
import com.k9x.application.events.use_case.CreateEventServiceCase;
import com.k9x.application.events.use_case.DeleteEventServiceCase;
import com.k9x.application.events.use_case.EnrollEventServiceCase;
import com.k9x.application.events.use_case.GetEventServiceCase;
import com.k9x.application.users.port.GetUserInfoPersistencePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredEventUseCaseConfiguration {

    @Bean
    public CreateEventServiceCase createEventServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                         SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        return new CreateEventServiceCase(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Bean
    public DeleteEventServiceCase deleteEventServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                         SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        return new DeleteEventServiceCase(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Bean
    public UpdateObdxEventServiceCase updateEventServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                             SaveCompetitionPersistencePort saveCompetitionPersistencePort,
                                                             GetObdxClassificationConfigPort getObdxClassificationConfigPort,
                                                             GetUserInfoPersistencePort getUserInfoPersistencePort) {
        return new UpdateObdxEventServiceCase(getCompetitionPersistencePort, saveCompetitionPersistencePort,
                getObdxClassificationConfigPort, getUserInfoPersistencePort);
    }

    @Bean
    public EnrollEventServiceCase enrollEventServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                         SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        return new EnrollEventServiceCase(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Bean
    public GetEventServiceCase getEventServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                   GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort) {
        return new GetEventServiceCase(getCompetitionPersistencePort, getObdxFederationsConfigurationsPort);
    }

    @Bean
    public UpdateObdxScoreServiceCase updateScoreServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                             GetObdxEventCollectorPersistencePort getObdxEventCollectorPersistencePort,
                                                             GetObdxExerciseAllowedValuesPort getObdxExerciseAllowedValuesPort,
                                                             SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        return new UpdateObdxScoreServiceCase(getCompetitionPersistencePort,
                getObdxEventCollectorPersistencePort, getObdxExerciseAllowedValuesPort, saveCompetitionPersistencePort);
    }
}
