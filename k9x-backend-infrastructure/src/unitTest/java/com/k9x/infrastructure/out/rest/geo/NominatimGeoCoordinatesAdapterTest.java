package com.k9x.infrastructure.out.rest.geo;

import com.k9x.application.competitions.use_case.dto.Coordinates;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class NominatimGeoCoordinatesAdapterTest {

    private static final String ADDRESS = "Rúa Anxo Senra Fernández, 24, 15670 Culleredo, A Coruña";
    private static final String USER_AGENT = "k9x-backend/test (txomin.sirera@clarity.ai)";

    private HttpServer server;
    private String baseUrl;
    private final AtomicReference<HttpExchange> lastExchange = new AtomicReference<>();
    private volatile int statusCode = 200;
    private volatile String responseBody = "[]";

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/search", exchange -> {
            lastExchange.set(exchange);
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/search";
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    private NominatimGeoCoordinatesAdapter adapter() {
        return new NominatimGeoCoordinatesAdapter(USER_AGENT, baseUrl, HttpClient.newHttpClient());
    }

    @Test
    void returns_coordinates_when_nominatim_finds_the_address() {
        statusCode = 200;
        responseBody = "[{\"lat\":\"43.3184118\",\"lon\":\"-8.3660247\","
                + "\"display_name\":\"Rúa Anxo Senra Fernández, Culleredo, A Coruña\"}]";

        Coordinates coordinates = adapter().getCoordinates(ADDRESS);

        assertThat(coordinates.coordAlt()).isEqualTo(43.3184118);
        assertThat(coordinates.coordLong()).isEqualTo(-8.3660247);
    }

    @Test
    void sends_the_address_as_a_free_form_query_with_the_user_agent() {
        statusCode = 200;
        responseBody = "[{\"lat\":\"43.3184118\",\"lon\":\"-8.3660247\"}]";

        adapter().getCoordinates(ADDRESS);

        HttpExchange exchange = lastExchange.get();
        assertThat(exchange.getRequestURI().getRawQuery())
                .contains("q=" + java.net.URLEncoder.encode(ADDRESS, StandardCharsets.UTF_8))
                .contains("format=jsonv2")
                .contains("limit=1");
        assertThat(exchange.getRequestHeaders().getFirst("User-Agent")).isEqualTo(USER_AGENT);
    }

    @Test
    void returns_null_coordinates_when_nominatim_returns_no_results() {
        statusCode = 200;
        responseBody = "[]";

        Coordinates coordinates = adapter().getCoordinates(ADDRESS);

        assertThat(coordinates.coordAlt()).isNull();
        assertThat(coordinates.coordLong()).isNull();
    }

    @Test
    void returns_null_coordinates_when_response_is_not_a_success() {
        statusCode = 403;
        responseBody = "blocked";

        Coordinates coordinates = adapter().getCoordinates(ADDRESS);

        assertThat(coordinates.coordAlt()).isNull();
        assertThat(coordinates.coordLong()).isNull();
    }

    @Test
    void returns_null_coordinates_when_address_is_blank_without_calling_nominatim() {
        Coordinates coordinates = adapter().getCoordinates("   ");

        assertThat(coordinates.coordAlt()).isNull();
        assertThat(coordinates.coordLong()).isNull();
        assertThat(lastExchange.get()).isNull();
    }
}
