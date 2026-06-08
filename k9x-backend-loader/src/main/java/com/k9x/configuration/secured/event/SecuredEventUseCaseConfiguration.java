package com.k9x.configuration.secured.event;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.disciplines.obdx.port.GetObdxExerciseAllowedValuesPort;
import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.events.obdx.port.*;
import com.k9x.application.events.obdx.use_case.UpdateObdxEventServiceCase;
import com.k9x.application.events.obdx.use_case.UpdateObdxScoreServiceCase;
import com.k9x.application.events.obdx.use_case.port.CreateObdxEventPersistencePort;
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
                                                         CreateObdxEventPersistencePort createObdxEventPersistencePort) {
        return new CreateEventServiceCase(getCompetitionPersistencePort, createObdxEventPersistencePort);
    }

    @Bean
    public DeleteEventServiceCase deleteEventServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                         DeleteObdxEventPersistencePort deleteObdxEventPersistencePort) {
        return new DeleteEventServiceCase(getCompetitionPersistencePort, deleteObdxEventPersistencePort);
    }

    @Bean
    public UpdateObdxEventServiceCase updateEventServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                             UpdateObdxEventPersistencePort updateObdxEventPersistencePort,
                                                             GetObdxClassificationConfigPort getObdxClassificationConfigPort,
                                                             GetUserInfoPersistencePort getUserInfoPersistencePort) {
        return new UpdateObdxEventServiceCase(getCompetitionPersistencePort, updateObdxEventPersistencePort,
                getObdxClassificationConfigPort, getUserInfoPersistencePort);
    }

    @Bean
    public EnrollEventServiceCase enrollEventServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                         EnrollObdxEventPersistencePort enrollObdxEventPersistencePort) {
        return new EnrollEventServiceCase(getCompetitionPersistencePort, enrollObdxEventPersistencePort);
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
                                                             UpdateObdxScorePersistencePort updateObdxScorePersistencePort) {
        return new UpdateObdxScoreServiceCase(getCompetitionPersistencePort,
                getObdxEventCollectorPersistencePort, getObdxExerciseAllowedValuesPort, updateObdxScorePersistencePort);
    }
}
