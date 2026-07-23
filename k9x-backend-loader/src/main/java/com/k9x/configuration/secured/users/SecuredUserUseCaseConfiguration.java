package com.k9x.configuration.secured.users;

import com.k9x.application.users.port.RegisterPushSubscriptionPersistencePort;
import com.k9x.application.users.use_case.RegisterPushSubscriptionServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredUserUseCaseConfiguration {

    @Bean
    public RegisterPushSubscriptionServiceCase registerPushSubscriptionServiceCase(
            RegisterPushSubscriptionPersistencePort registerPushSubscriptionPersistencePort) {
        return new RegisterPushSubscriptionServiceCase(registerPushSubscriptionPersistencePort);
    }
}
