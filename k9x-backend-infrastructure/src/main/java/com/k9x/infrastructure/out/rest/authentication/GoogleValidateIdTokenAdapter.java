package com.k9x.infrastructure.out.rest.authentication;

import com.k9x.application.authentication.port.ValidateIdTokenPort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GoogleValidateIdTokenAdapter implements ValidateIdTokenPort {

    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean isValid(String idToken) {
        String url = UriComponentsBuilder.fromUriString(GOOGLE_TOKEN_INFO_URL)
                .queryParam("id_token", idToken)
                .toUriString();

        try {
            restTemplate.getForEntity(url, String.class);
            return true;
        } catch (RestClientResponseException ex) {
            return false;
        }
    }
}
