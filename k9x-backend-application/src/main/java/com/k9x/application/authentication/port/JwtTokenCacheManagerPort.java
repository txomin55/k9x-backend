package com.k9x.application.authentication.port;

import com.k9x.application.authentication.dto.AuthTokenDTO;

public interface JwtTokenCacheManagerPort {

    void overrideEntry(String id, String jwtToken);

    AuthTokenDTO retrieveEntry(String id);
}
