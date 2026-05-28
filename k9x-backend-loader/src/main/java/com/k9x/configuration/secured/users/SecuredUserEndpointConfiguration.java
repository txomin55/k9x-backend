package com.k9x.configuration.secured.users;

import com.k9x.application.users.use_case.LogoutServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.endpoints.secured.users.GetUserData;
import com.k9x.infrastructure.in.rest.endpoints.secured.users.Logout;
import com.k9x.infrastructure.in.rest.endpoints.secured.users.RegisterPush;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredUserEndpointConfiguration {

    @Bean
    public Logout logout(LogoutServiceCase logoutServiceCase, UserInfoDTO userInfoDTO) {
        return new Logout(logoutServiceCase, userInfoDTO);
    }

    @Bean
    public RegisterPush registerPush() {
        return new RegisterPush();
    }

    @Bean
    public GetUserData getUserData(UserInfoDTO userInfoDTO) {
        return new GetUserData(userInfoDTO);
    }
}
