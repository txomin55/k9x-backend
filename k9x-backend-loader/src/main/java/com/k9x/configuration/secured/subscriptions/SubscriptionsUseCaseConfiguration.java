package com.k9x.configuration.secured.subscriptions;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.subscriptions.port.CreateUserSubscriptionsPersistencePort;
import com.k9x.application.subscriptions.port.GetUserSubscriptionsPersistencePort;
import com.k9x.application.subscriptions.port.UpdateUserSubscriptionPersistencePort;
import com.k9x.application.subscriptions.use_case.GetUserSubscriptionsServiceCase;
import com.k9x.application.subscriptions.use_case.UpdateUserSubscriptionServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SubscriptionsUseCaseConfiguration {

    @Bean
    public UpdateUserSubscriptionServiceCase updateUserSubscriptionServiceCase(
            GetCompetitionPersistencePort getCompetitionPersistencePort,
            CreateUserSubscriptionsPersistencePort createUserSubscriptionsPersistencePort,
            UpdateUserSubscriptionPersistencePort updateUserSubscriptionPersistencePort) {
        return new UpdateUserSubscriptionServiceCase(getCompetitionPersistencePort,
                createUserSubscriptionsPersistencePort, updateUserSubscriptionPersistencePort);
    }

    @Bean
    public GetUserSubscriptionsServiceCase getUserSubscriptionsServiceCase(
            GetUserSubscriptionsPersistencePort getUserSubscriptionsPersistencePort) {
        return new GetUserSubscriptionsServiceCase(getUserSubscriptionsPersistencePort);
    }
}
