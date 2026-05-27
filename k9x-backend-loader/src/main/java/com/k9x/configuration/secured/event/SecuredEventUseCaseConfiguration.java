package com.k9x.configuration.secured.event;

import com.k9x.application.events.obdx.port.CreateObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.DeleteObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.GetObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.UpdateObdxEventPersistencePort;
import com.k9x.application.events.obdx.use_case.CreateObdxEventServiceCase;
import com.k9x.application.events.obdx.use_case.DeleteObdxEventServiceCase;
import com.k9x.application.events.obdx.use_case.UpdateObdxEventServiceCase;
import com.k9x.application.stages.port.GetStagePersistencePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredEventUseCaseConfiguration {

    @Bean
    public CreateObdxEventServiceCase createEventServiceCase(GetStagePersistencePort getStagePersistencePort,
                                                             CreateObdxEventPersistencePort createObdxEventPersistencePort) {
        return new CreateObdxEventServiceCase(getStagePersistencePort, createObdxEventPersistencePort);
    }

    @Bean
    public DeleteObdxEventServiceCase deleteEventServiceCase(GetObdxEventPersistencePort getObdxEventPersistencePort,
                                                             GetStagePersistencePort getStagePersistencePort,
                                                             DeleteObdxEventPersistencePort deleteObdxEventPersistencePort) {
        return new DeleteObdxEventServiceCase(getObdxEventPersistencePort, getStagePersistencePort, deleteObdxEventPersistencePort);
    }

    @Bean
    public UpdateObdxEventServiceCase updateEventServiceCase(GetObdxEventPersistencePort getObdxEventPersistencePort,
                                                             UpdateObdxEventPersistencePort updateObdxEventPersistencePort) {
        return new UpdateObdxEventServiceCase(getObdxEventPersistencePort, updateObdxEventPersistencePort);
    }
}
