package com.k9x.infrastructure.in.rest.configuration.session.user;

import com.k9x.infrastructure.in.rest.configuration.session.user.dto.RequestUserDetails;

public interface AuthorizationExtractor {

    RequestUserDetails getDataFromToken(String authorization);
}
