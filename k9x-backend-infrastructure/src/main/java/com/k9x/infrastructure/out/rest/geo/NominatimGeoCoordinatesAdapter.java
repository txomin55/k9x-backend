package com.k9x.infrastructure.out.rest.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.competitions.port.GeoCoordinatesPort;
import com.k9x.application.competitions.use_case.dto.Coordinates;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class NominatimGeoCoordinatesAdapter implements GeoCoordinatesPort {

    // https://nominatim.org/release-docs/develop/api/Search/#free-form-query
    private static final String DEFAULT_SEARCH_ENDPOINT = "https://nominatim.openstreetmap.org/search";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String userAgent;
    private final String searchEndpoint;

    public NominatimGeoCoordinatesAdapter(String userAgent) {
        this(userAgent, DEFAULT_SEARCH_ENDPOINT, HttpClient.newHttpClient());
    }

    NominatimGeoCoordinatesAdapter(String userAgent, String searchEndpoint, HttpClient httpClient) {
        this.userAgent = userAgent;
        this.searchEndpoint = searchEndpoint;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Coordinates getCoordinates(String address) {
        if (address == null || address.isBlank()) {
            return new Coordinates(null, null);
        }

        URI uri = URI.create(searchEndpoint
                + "?q=" + urlEncode(address)
                + "&format=jsonv2"
                + "&limit=1");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                // Nominatim's usage policy requires an identifying User-Agent.
                .header("User-Agent", userAgent)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return new Coordinates(null, null);
            }

            JsonNode results = objectMapper.readTree(response.body());
            if (!results.isArray() || results.isEmpty()) {
                return new Coordinates(null, null);
            }

            JsonNode first = results.get(0);
            JsonNode latNode = first.get("lat");
            JsonNode lonNode = first.get("lon");
            if (latNode == null || latNode.isNull() || lonNode == null || lonNode.isNull()) {
                return new Coordinates(null, null);
            }

            return new Coordinates(
                    Double.parseDouble(latNode.asText()),
                    Double.parseDouble(lonNode.asText()));
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            return new Coordinates(null, null);
        } catch (IOException | NumberFormatException _) {
            return new Coordinates(null, null);
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
