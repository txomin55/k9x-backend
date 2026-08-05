package com.k9x.configuration.secured.subscriptions;

import com.k9x.application.subscriptions.use_case.UpdateUserSubscriptionServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.endpoints.secured.subscriptions.UpdateSubscription;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredSubscriptionsEndpointConfiguration {

    @Bean
    public UpdateSubscription updateSubscription(
            UpdateUserSubscriptionServiceCase updateUserSubscriptionServiceCase, UserInfoDTO userInfoDTO) {
        return new UpdateSubscription(updateUserSubscriptionServiceCase, userInfoDTO);
    }
}
