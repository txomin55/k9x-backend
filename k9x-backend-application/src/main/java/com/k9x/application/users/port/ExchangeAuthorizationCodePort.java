package com.k9x.application.users.port;

import java.util.Optional;

public interface ExchangeAuthorizationCodePort {

    Optional<String> exchangeForIdToken(String authorizationCode);
}
