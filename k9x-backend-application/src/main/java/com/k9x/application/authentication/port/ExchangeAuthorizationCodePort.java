package com.k9x.application.authentication.port;

import java.util.Optional;

public interface ExchangeAuthorizationCodePort {

    Optional<String> exchangeForIdToken(String authorizationCode);
}
