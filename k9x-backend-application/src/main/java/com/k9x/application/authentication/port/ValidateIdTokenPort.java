package com.k9x.application.authentication.port;

public interface ValidateIdTokenPort {

    boolean isValid(String idToken);
}
