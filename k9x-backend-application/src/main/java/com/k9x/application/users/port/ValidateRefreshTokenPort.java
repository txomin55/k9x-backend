package com.k9x.application.users.port;

import java.util.Optional;

public interface ValidateRefreshTokenPort {

    Optional<String> getSubjectIfValid(String refreshToken);
}
