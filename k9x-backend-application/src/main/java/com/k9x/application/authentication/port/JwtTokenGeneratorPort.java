package com.k9x.application.authentication.port;

import java.time.Duration;

public interface JwtTokenGeneratorPort {
    String generate(String subject, int version, Duration ttl);
}
