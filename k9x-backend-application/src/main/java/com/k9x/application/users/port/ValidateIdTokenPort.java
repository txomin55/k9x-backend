package com.k9x.application.users.port;

import com.k9x.application.users.use_case.dto.ValidatedIdTokenDTO;

import java.util.Optional;

public interface ValidateIdTokenPort {

    Optional<ValidatedIdTokenDTO> getUserIfValid(String idToken);
}
