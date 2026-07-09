package com.k9x.infrastructure.out.rest.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.k9x.application.users.port.ExchangeAuthorizationCodePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class GoogleExchangeAuthorizationCodeAdapter implements ExchangeAuthorizationCodePort {

    private static final Logger log = LoggerFactory.getLogger(GoogleExchangeAuthorizationCodeAdapter.class);
    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

    private final HttpClient httpClient;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public GoogleExchangeAuthorizationCodeAdapter(
            String clientId,
            String clientSecret,
            String redirectUri
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public Optional<String> exchangeForIdToken(String authorizationCode) {
        if (authorizationCode == null || authorizationCode.isBlank()) {
            log.warn("Google code exchange skipped: blank authorization code");
            return Optional.empty();
        }

        String requestBody = "code=" + urlEncode(authorizationCode)
                + "&client_id=" + urlEncode(clientId)
                + "&client_secret=" + urlEncode(clientSecret)
                + "&redirect_uri=" + urlEncode(redirectUri)
                + "&grant_type=authorization_code";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_ENDPOINT))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Google code exchange failed: status={} redirect_uri={} body={}",
                        response.statusCode(), redirectUri, response.body());
                return Optional.empty();
            }
            JsonNode payload = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response.body());
            JsonNode idTokenNode = payload.get("id_token");
            if (idTokenNode == null || idTokenNode.isNull()) {
                log.warn("Google code exchange succeeded but response has no id_token: body={}", response.body());
                return Optional.empty();
            }
            String idToken = idTokenNode.asText();
            return idToken == null || idToken.isBlank() ? Optional.empty() : Optional.of(idToken);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Google code exchange interrupted", e);
            return Optional.empty();
        } catch (IOException e) {
            log.warn("Google code exchange I/O error", e);
            return Optional.empty();
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
