package com.k9x.infrastructure.in.rest.configuration.session;

import com.k9x.application.users.use_case.dto.UserInfoDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;

import static com.k9x.infrastructure.in.rest.configuration.filter.Auth.USER_DETAILS;

/**
 * The authenticated user of the current request, if there is one.
 *
 * <p>Public endpoints cannot inject the request-scoped {@code UserInfoDTO} bean: it throws when nobody is
 * authenticated, which is the normal case for them. This reads the same request attribute the auth filter
 * sets and simply reports its absence instead.
 */
@Component
public class OptionalRequestUser {

    public Optional<UserInfoDTO> current() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return Optional.empty();
        }
        Object value = attributes.getAttribute(USER_DETAILS, RequestAttributes.SCOPE_REQUEST);
        return value instanceof UserInfoDTO userInfo ? Optional.of(userInfo) : Optional.empty();
    }

    public boolean isAuthenticated() {
        return current().isPresent();
    }
}
