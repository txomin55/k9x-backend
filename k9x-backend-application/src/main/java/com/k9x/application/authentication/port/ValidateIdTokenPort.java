package com.k9x.application.authentication.port;

import java.util.Optional;

public interface ValidateIdTokenPort {

    Optional<String> getEmailIfValid(String idToken);
}
