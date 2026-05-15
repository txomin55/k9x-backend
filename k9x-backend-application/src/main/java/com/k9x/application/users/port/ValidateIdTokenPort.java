package com.k9x.application.users.port;

import java.util.Optional;

public interface ValidateIdTokenPort {

    Optional<String> getEmailIfValid(String idToken);
}
