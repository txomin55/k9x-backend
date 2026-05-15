package com.k9x.configuration.users;

import com.k9x.application.users.use_case.LoginServiceCase;
import com.k9x.infrastructure.in.rest.endpoints.users.Login;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsersEndpointConfiguration {

    @Bean
    public Login login(LoginServiceCase loginServiceCase) {
        return new Login(loginServiceCase);
    }
}
