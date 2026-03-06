package com.k9x.infrastructure.out.rest.authentication;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.k9x.application.authentication.port.ValidateIdTokenPort;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GoogleValidateIdTokenAdapter implements ValidateIdTokenPort {

    private final GoogleIdTokenVerifier verifier;

    public GoogleValidateIdTokenAdapter(
            String googleClientId
    ) {
        List<String> audiences = Arrays.stream(googleClientId.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();

        try {

            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            this.verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    jsonFactory
            ).setAudience(audiences).build();

        } catch (GeneralSecurityException | IOException ex) {
            throw new IllegalStateException("Failed to initialize Google ID token verifier", ex);
        }
    }

    @Override
    public Optional<String> getEmailIfValid(String idToken) {
        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null || token.getPayload() == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(token.getPayload().getEmail());
        } catch (Exception _) {
            return Optional.empty();
        }
    }
}
