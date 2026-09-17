package com.k9x.configuration.secured.users;

import com.k9x.application.users.port.GetUserNotificationsEnabledPersistencePort;
import com.k9x.application.users.port.RegisterPushSubscriptionPersistencePort;
import com.k9x.application.users.port.SetUserNotificationsEnabledPersistencePort;
import com.k9x.application.users.use_case.GetNotificationsEnabledServiceCase;
import com.k9x.application.users.use_case.RegisterPushSubscriptionServiceCase;
import com.k9x.application.users.use_case.SetNotificationsEnabledServiceCase;
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
    public GetNotificationsEnabledServiceCase getNotificationsEnabledServiceCase(
            GetUserNotificationsEnabledPersistencePort getUserNotificationsEnabledPersistencePort) {
        return new GetNotificationsEnabledServiceCase(getUserNotificationsEnabledPersistencePort);
    }

    @Bean
    public SetNotificationsEnabledServiceCase setNotificationsEnabledServiceCase(
            SetUserNotificationsEnabledPersistencePort setUserNotificationsEnabledPersistencePort) {
        return new SetNotificationsEnabledServiceCase(setUserNotificationsEnabledPersistencePort);
    }
}
