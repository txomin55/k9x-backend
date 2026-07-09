package com.k9x.infrastructure.out.rest.authentication;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.k9x.application.users.port.ValidateIdTokenPort;
import com.k9x.application.users.use_case.dto.ValidatedIdTokenDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GoogleValidateIdTokenAdapter implements ValidateIdTokenPort {

    private static final Logger log = LoggerFactory.getLogger(GoogleValidateIdTokenAdapter.class);

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
    public Optional<ValidatedIdTokenDTO> getUserIfValid(String idToken) {
        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null || token.getPayload() == null) {
                log.warn("Google id_token validation failed: token null or unverified (audience mismatch or bad signature?)");
                return Optional.empty();
            }
            GoogleIdToken.Payload payload = token.getPayload();
            String email = payload.getEmail();
            if (email == null) {
                log.warn("Google id_token validation failed: no email in payload");
                return Optional.empty();
            }
            Object picture = payload.get("picture");
            String image = picture != null ? picture.toString() : "";
            return Optional.of(new ValidatedIdTokenDTO(email, image));
        } catch (Exception e) {
            log.warn("Google id_token validation threw", e);
            return Optional.empty();
        }
    }
}
