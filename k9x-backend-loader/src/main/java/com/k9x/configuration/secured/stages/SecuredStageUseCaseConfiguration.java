package com.k9x.configuration.secured.stages;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.notifications.port.GetEventRecipientsPersistencePort;
import com.k9x.application.notifications.port.PushNotifier;
import com.k9x.application.notifications.port.SaveEventNotificationPersistencePort;
import com.k9x.application.notifications.port.SaveNotificationPersistencePort;
import com.k9x.application.notifications.use_case.CreateStageNotificationsServiceCase;
import com.k9x.application.stages.use_case.CreateStageServiceCase;
import com.k9x.application.stages.use_case.DeleteStageServiceCase;
import com.k9x.application.stages.use_case.UpdateStageServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredStageUseCaseConfiguration {

    @Bean
    public CreateStageServiceCase createStageServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                         SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        return new CreateStageServiceCase(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Bean
    public UpdateStageServiceCase updateStageServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                         SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        return new UpdateStageServiceCase(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Bean
    public DeleteStageServiceCase deleteStageServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                         SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        return new DeleteStageServiceCase(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Bean
    public CreateStageNotificationsServiceCase createStageNotificationsServiceCase(
            GetCompetitionPersistencePort getCompetitionPersistencePort,
            SaveEventNotificationPersistencePort saveEventNotificationPersistencePort,
            GetEventRecipientsPersistencePort getEventRecipientsPersistencePort,
            SaveNotificationPersistencePort saveNotificationPersistencePort,
            PushNotifier pushNotifier) {
        return new CreateStageNotificationsServiceCase(getCompetitionPersistencePort,
                saveEventNotificationPersistencePort, getEventRecipientsPersistencePort,
                saveNotificationPersistencePort, pushNotifier);
    }
}
