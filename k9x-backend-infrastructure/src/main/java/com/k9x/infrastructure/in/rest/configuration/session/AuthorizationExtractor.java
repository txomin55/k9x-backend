package com.k9x.infrastructure.in.rest.configuration.session;

import com.k9x.application.users.use_case.dto.AuthTokenDTO;

public interface AuthorizationExtractor {

    AuthTokenDTO getDataFromToken(String token);
}
