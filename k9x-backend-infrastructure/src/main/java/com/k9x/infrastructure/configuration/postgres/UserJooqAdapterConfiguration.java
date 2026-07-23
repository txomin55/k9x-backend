package com.k9x.infrastructure.configuration.postgres;

import com.k9x.application.users.port.CreateUserPersistencePort;
import com.k9x.application.users.port.DeletePushSubscriptionPersistencePort;
import com.k9x.application.users.port.GetPushSubscriptionsPersistencePort;
import com.k9x.application.users.port.GetUserInfoPersistencePort;
import com.k9x.application.users.port.RegisterPushSubscriptionPersistencePort;
import com.k9x.infrastructure.out.postgres.users.CreateUserJooqAdapter;
import com.k9x.infrastructure.out.postgres.users.DeletePushSubscriptionJooqAdapter;
import com.k9x.infrastructure.out.postgres.users.GetPushSubscriptionsJooqAdapter;
import com.k9x.infrastructure.out.postgres.users.GetUserInfoJooqAdapter;
import com.k9x.infrastructure.out.postgres.users.RegisterPushSubscriptionJooqAdapter;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserJooqAdapterConfiguration {

    @Bean
    public GetUserInfoPersistencePort getUserInfoPersistencePort(DSLContext dsl) {
        return new GetUserInfoJooqAdapter(dsl);
    }

    @Bean
    public CreateUserPersistencePort createUserPersistencePort(DSLContext dsl) {
        return new CreateUserJooqAdapter(dsl);
    }

    @Bean
    public RegisterPushSubscriptionPersistencePort registerPushSubscriptionPersistencePort(DSLContext dsl) {
        return new RegisterPushSubscriptionJooqAdapter(dsl);
    }

    @Bean
    public GetPushSubscriptionsPersistencePort getPushSubscriptionsPersistencePort(DSLContext dsl) {
        return new GetPushSubscriptionsJooqAdapter(dsl);
    }

    @Bean
    public DeletePushSubscriptionPersistencePort deletePushSubscriptionPersistencePort(DSLContext dsl) {
        return new DeletePushSubscriptionJooqAdapter(dsl);
    }
}
