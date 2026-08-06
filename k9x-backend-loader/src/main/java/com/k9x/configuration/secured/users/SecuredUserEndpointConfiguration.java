package com.k9x.configuration.secured.users;

import com.k9x.application.subscriptions.use_case.GetUserSubscriptionsServiceCase;
import com.k9x.application.users.use_case.LogoutServiceCase;
import com.k9x.application.users.use_case.RegisterPushSubscriptionServiceCase;
import com.k9x.application.users.use_case.RemovePushSubscriptionServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.configuration.session.RefreshTokenCookie;
import com.k9x.infrastructure.in.rest.endpoints.secured.users.GetUserData;
import com.k9x.infrastructure.in.rest.endpoints.secured.users.Logout;
import com.k9x.infrastructure.in.rest.endpoints.secured.users.RegisterPush;
import com.k9x.infrastructure.in.rest.endpoints.secured.users.RemovePush;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredUserEndpointConfiguration {

    @Bean
    public Logout logout(LogoutServiceCase logoutServiceCase, UserInfoDTO userInfoDTO, RefreshTokenCookie refreshTokenCookie) {
        return new Logout(logoutServiceCase, userInfoDTO, refreshTokenCookie);
    }

    @Bean
    public RegisterPush registerPush(RegisterPushSubscriptionServiceCase registerPushSubscriptionServiceCase, UserInfoDTO userInfoDTO) {
        return new RegisterPush(registerPushSubscriptionServiceCase, userInfoDTO);
    }

    @Bean
    public RemovePush removePush(RemovePushSubscriptionServiceCase removePushSubscriptionServiceCase, UserInfoDTO userInfoDTO) {
        return new RemovePush(removePushSubscriptionServiceCase, userInfoDTO);
    }

    @Bean
    public GetUserData getUserData(GetUserSubscriptionsServiceCase getUserSubscriptionsServiceCase, UserInfoDTO userInfoDTO) {
        return new GetUserData(getUserSubscriptionsServiceCase, userInfoDTO);
    }
}
