package com.k9x.application.users.port;

import com.k9x.application.users.use_case.dto.AuthTokenDTO;

public interface JwtTokenCacheManagerPort {

    void overrideEntry(String id, String jwtToken);

    AuthTokenDTO retrieveEntry(String id);

    void deleteEntry(String id);
}
