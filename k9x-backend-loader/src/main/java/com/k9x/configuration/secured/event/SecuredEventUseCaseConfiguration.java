package com.k9x.configuration.secured.event;

import com.k9x.application.disciplines.obdx.port.GetObdxExerciseAllowedValuesPort;
import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.events.obdx.port.*;
import com.k9x.application.events.obdx.use_case.UpdateObdxEventServiceCase;
import com.k9x.application.events.obdx.use_case.UpdateObdxScoreServiceCase;
import com.k9x.application.events.obdx.use_case.port.CreateObdxEventPersistencePort;
import com.k9x.application.events.obdx.use_case.port.GetEventPersistencePort;
import com.k9x.application.events.obdx.use_case.port.GetObdxEventDataPersistencePort;
import com.k9x.application.events.use_case.CreateEventServiceCase;
import com.k9x.application.events.use_case.DeleteEventServiceCase;
import com.k9x.application.events.use_case.EnrollEventServiceCase;
import com.k9x.application.events.use_case.GetEventServiceCase;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.application.users.port.GetUserInfoPersistencePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredEventUseCaseConfiguration {

    @Bean
    public CreateEventServiceCase createEventServiceCase(GetStagePersistencePort getStagePersistencePort,
                                                         CreateObdxEventPersistencePort createObdxEventPersistencePort) {
        return new CreateEventServiceCase(getStagePersistencePort, createObdxEventPersistencePort);
    }

    @Bean
    public DeleteEventServiceCase deleteEventServiceCase(GetEventPersistencePort getEventPersistencePort,
                                                         GetStagePersistencePort getStagePersistencePort,
                                                         DeleteObdxEventPersistencePort deleteObdxEventPersistencePort) {
        return new DeleteEventServiceCase(getEventPersistencePort, getStagePersistencePort, deleteObdxEventPersistencePort);
    }

    @Bean
    public UpdateObdxEventServiceCase updateEventServiceCase(GetEventPersistencePort getEventPersistencePort,
                                                             UpdateObdxEventPersistencePort updateObdxEventPersistencePort,
                                                             GetObdxClassificationConfigPort getObdxClassificationConfigPort,
                                                             GetUserInfoPersistencePort getUserInfoPersistencePort) {
        return new UpdateObdxEventServiceCase(getEventPersistencePort, updateObdxEventPersistencePort,
                getObdxClassificationConfigPort, getUserInfoPersistencePort);
    }

    @Bean
    public EnrollEventServiceCase enrollEventServiceCase(GetEventPersistencePort getEventPersistencePort,
                                                         GetStagePersistencePort getStagePersistencePort,
                                                         EnrollObdxEventPersistencePort enrollObdxEventPersistencePort) {
        return new EnrollEventServiceCase(getEventPersistencePort, getStagePersistencePort, enrollObdxEventPersistencePort);
    }

    @Bean
    public GetEventServiceCase getEventServiceCase(GetEventPersistencePort getEventPersistencePort,
                                                   GetStagePersistencePort getStagePersistencePort,
                                                   GetObdxEventDataPersistencePort getObdxEventDataPersistencePort,
                                                   GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort) {
        return new GetEventServiceCase(getEventPersistencePort, getStagePersistencePort,
                getObdxEventDataPersistencePort, getObdxFederationsConfigurationsPort);
    }

    @Bean
    public UpdateObdxScoreServiceCase updateScoreServiceCase(GetEventPersistencePort getEventPersistencePort,
                                                             GetStagePersistencePort getStagePersistencePort,
                                                             GetObdxEventCollectorPersistencePort getObdxEventCollectorPersistencePort,
                                                             GetObdxExerciseAllowedValuesPort getObdxExerciseAllowedValuesPort,
                                                             UpdateObdxScorePersistencePort updateObdxScorePersistencePort) {
        return new UpdateObdxScoreServiceCase(getEventPersistencePort, getStagePersistencePort,
                getObdxEventCollectorPersistencePort, getObdxExerciseAllowedValuesPort, updateObdxScorePersistencePort);
    }
}
