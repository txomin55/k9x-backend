package com.k9x.configuration.secured.users;

import com.k9x.application.users.port.DeletePushSubscriptionPersistencePort;
import com.k9x.application.users.port.RegisterPushSubscriptionPersistencePort;
import com.k9x.application.users.use_case.RegisterPushSubscriptionServiceCase;
import com.k9x.application.users.use_case.RemovePushSubscriptionServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredUserUseCaseConfiguration {

    @Bean
    public RegisterPushSubscriptionServiceCase registerPushSubscriptionServiceCase(
            RegisterPushSubscriptionPersistencePort registerPushSubscriptionPersistencePort) {
        return new RegisterPushSubscriptionServiceCase(registerPushSubscriptionPersistencePort);
    }

    @Bean
    public RemovePushSubscriptionServiceCase removePushSubscriptionServiceCase(
            DeletePushSubscriptionPersistencePort deletePushSubscriptionPersistencePort) {
        return new RemovePushSubscriptionServiceCase(deletePushSubscriptionPersistencePort);
    }
}
