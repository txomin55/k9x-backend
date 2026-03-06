package com.k9x.infrastructure.out.rest.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.k9x.application.authentication.port.ExchangeAuthorizationCodePort;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class GoogleExchangeAuthorizationCodeAdapter implements ExchangeAuthorizationCodePort {

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
                return Optional.empty();
            }
            JsonNode payload = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response.body());
            JsonNode idTokenNode = payload.get("id_token");
            if (idTokenNode == null || idTokenNode.isNull()) {
                return Optional.empty();
            }
            String idToken = idTokenNode.asText();
            return idToken == null || idToken.isBlank() ? Optional.empty() : Optional.of(idToken);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (IOException _) {
            return Optional.empty();
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
