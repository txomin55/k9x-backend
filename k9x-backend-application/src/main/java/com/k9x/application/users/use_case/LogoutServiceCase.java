package com.k9x.application.users.use_case;

import com.k9x.application.users.port.JwtTokenCacheManagerPort;

public class LogoutServiceCase {

    private final JwtTokenCacheManagerPort jwtTokenCacheManagerPort;

    public LogoutServiceCase(JwtTokenCacheManagerPort jwtTokenCacheManagerPort) {
        this.jwtTokenCacheManagerPort = jwtTokenCacheManagerPort;
    }

    public void logout(String userEmail) {
        jwtTokenCacheManagerPort.deleteEntry(userEmail);
    }
}
