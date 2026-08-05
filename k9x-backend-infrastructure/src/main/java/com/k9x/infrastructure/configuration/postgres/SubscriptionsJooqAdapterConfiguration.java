package com.k9x.infrastructure.configuration.postgres;

import com.k9x.application.subscriptions.port.CreateUserSubscriptionsPersistencePort;
import com.k9x.application.subscriptions.port.GetUserSubscriptionsPersistencePort;
import com.k9x.application.subscriptions.port.UpdateUserSubscriptionPersistencePort;
import com.k9x.infrastructure.out.postgres.subscriptions.CreateUserSubscriptionsJooqAdapter;
import com.k9x.infrastructure.out.postgres.subscriptions.GetUserSubscriptionsJooqAdapter;
import com.k9x.infrastructure.out.postgres.subscriptions.UpdateUserSubscriptionJooqAdapter;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SubscriptionsJooqAdapterConfiguration {

    @Bean
    public CreateUserSubscriptionsPersistencePort createUserSubscriptionsPersistencePort(DSLContext dsl) {
        return new CreateUserSubscriptionsJooqAdapter(dsl);
    }

    @Bean
    public GetUserSubscriptionsPersistencePort getUserSubscriptionsPersistencePort(DSLContext dsl) {
        return new GetUserSubscriptionsJooqAdapter(dsl);
    }

    @Bean
    public UpdateUserSubscriptionPersistencePort updateUserSubscriptionPersistencePort(DSLContext dsl) {
        return new UpdateUserSubscriptionJooqAdapter(dsl);
    }
}
