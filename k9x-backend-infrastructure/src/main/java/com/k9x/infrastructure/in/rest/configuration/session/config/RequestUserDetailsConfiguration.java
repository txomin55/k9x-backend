package com.k9x.infrastructure.in.rest.configuration.session.config;

import com.k9x.application.users.dto.AuthTokenDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.annotation.RequestScope;

import static com.k9x.infrastructure.in.rest.configuration.filter.Auth.USER_DETAILS;

@Configuration
public class RequestUserDetailsConfiguration {

    private final MessageSource messageSource;

    public RequestUserDetailsConfiguration(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Bean
    @RequestScope
    AuthTokenDTO requestUserDetails(HttpServletRequest request) {
        Object value = request.getAttribute(USER_DETAILS);
        if (value instanceof AuthTokenDTO userDetails) {
            return userDetails;
        }

        String message = messageSource.getMessage(
                "error.request_user_details_missing",
                null,
                LocaleContextHolder.getLocale()
        );
        throw new IllegalStateException(message);
    }
}
