package com.k9x.infrastructure.in.rest.endpoints.methodology;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.methodology.use_case.GetK9xMethodologyServiceCase;
import com.k9x.application.methodology.use_case.GetObdxMethodologyServiceCase;
import com.k9x.infrastructure.out.json.disciplines.obdx.ObdxFederationsConfigurationsCache;
import com.k9x.infrastructure.out.json.disciplines.obdx.ObdxJsonClassificationConfigAdapter;
import com.k9x.infrastructure.out.json.disciplines.obdx.ObdxJsonMethodologyCatalogAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The contract of the two methodology endpoints, run against the real configuration files and message bundles:
 * the serialized answer is compared with a golden file, so touching a band, a category sub-band, a curve or a
 * name makes this fail and forces the golden to be regenerated — the methodology pages never lie about the
 * calculation. To regenerate after a deliberate change, run with {@code METHODOLOGY_GOLDEN_UPDATE=true}.
 *
 * <p>Besides the snapshot, a few invariants of the contract are checked on the payload itself, since a golden
 * regenerated carelessly would otherwise absorb them.
 */
class MethodologyContractTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path GOLDEN_DIR = Path.of("src/unitTest/resources/methodology");

    private static final FetchObdxMethodology OBDX;
    private static final FetchK9xMethodology K9X;

    static {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        ObdxFederationsConfigurationsCache cache = new ObdxFederationsConfigurationsCache(new ObjectMapper());
        ObdxJsonMethodologyCatalogAdapter catalog = new ObdxJsonMethodologyCatalogAdapter(cache, messageSource);
        ObdxJsonClassificationConfigAdapter configs = new ObdxJsonClassificationConfigAdapter(cache);
        OBDX = new FetchObdxMethodology(new GetObdxMethodologyServiceCase(catalog, configs));
        K9X = new FetchK9xMethodology(new GetK9xMethodologyServiceCase(catalog, configs));
    }

    @Test
    void obdx_methodology_matches_its_golden() throws IOException {
        assertMatchesGolden("obdx.json", OBDX.fetchObdxMethodology());
    }

    @Test
    void k9x_methodology_matches_its_golden() throws IOException {
        assertMatchesGolden("k9x.json", K9X.fetchK9xMethodology());
    }

    @Test
    void both_answers_are_public_and_cacheable_for_a_day() {
        assertThat(OBDX.fetchObdxMethodology().getHeaders().getFirst(HttpHeaders.CACHE_CONTROL))
                .isEqualTo("max-age=86400, public");
        assertThat(K9X.fetchK9xMethodology().getHeaders().getFirst(HttpHeaders.CACHE_CONTROL))
                .isEqualTo("max-age=86400, public");
    }

    @Test
    void every_tier_score_lies_inside_its_sub_band_and_off_its_floor() throws IOException {
        JsonNode obdx = tree(OBDX.fetchObdxMethodology());
        for (JsonNode federation : obdx.get("federations")) {
            for (JsonNode grade : federation.get("grades")) {
                for (JsonNode category : grade.get("categories")) {
                    int min = category.get("subBand").get("min").asInt();
                    int max = category.get("subBand").get("max").asInt();
                    assertThat(category.get("tiers")).hasSize(3);
                    for (JsonNode tier : category.get("tiers")) {
                        int score = tier.get("rankScore").asInt();
                        assertThat(score).as("%s %s", grade.get("id"), category.get("id")).isBetween(min, max);
                        if (!category.get("fixed").asBoolean()) {
                            assertThat(score).as("%s %s", grade.get("id"), category.get("id")).isGreaterThan(min);
                        }
                    }
                }
            }
        }
    }

    @Test
    void the_knee_lies_on_the_curve_at_the_top_qualification() throws IOException {
        JsonNode meritCurve = tree(OBDX.fetchObdxMethodology()).get("meritCurve");
        JsonNode knee = series(meritCurve, "knee").get(0);
        int top = 0;
        for (JsonNode qualification : meritCurve.get("context").get("qualifications")) {
            if (qualification.get("top").asBoolean()) {
                top = qualification.get("score").asInt();
            }
        }
        assertThat(knee.get("x").asInt()).isEqualTo(top);
        boolean onCurve = false;
        for (JsonNode point : series(meritCurve, "curve")) {
            onCurve |= point.equals(knee);
        }
        assertThat(onCurve).isTrue();
    }

    @Test
    void every_example_event_lands_on_its_index_line() throws IOException {
        JsonNode example = tree(K9X.fetchK9xMethodology()).get("indexExample");
        int slots = example.get("parameters").get("N").asInt();
        assertThat(example.get("slotFilling").get("cases").size()).isGreaterThanOrEqualTo(slots + 1);
        example.get("slotFilling").get("cases").forEach(slotCase -> assertThat(slotCase.get("slots")).hasSize(slots));
        for (JsonNode dog : example.get("dogs")) {
            for (JsonNode event : dog.get("events")) {
                boolean onLine = false;
                for (JsonNode point : dog.get("series")) {
                    onLine |= point.get("month").equals(event.get("month")) && point.get("index").equals(event.get("index"));
                }
                assertThat(onLine).as("%s at month %s", dog.get("id"), event.get("month")).isTrue();
            }
        }
    }

    private static JsonNode series(JsonNode meritCurve, String id) {
        for (JsonNode series : meritCurve.get("series")) {
            if (series.get("id").asText().equals(id)) {
                return series.get("points");
            }
        }
        throw new AssertionError("No " + id + " series");
    }

    /** Through the serialized text, as a client reads it: numbers compare by value, not by Java type. */
    private static JsonNode tree(ResponseEntity<?> response) throws IOException {
        return MAPPER.readTree(MAPPER.writeValueAsString(response.getBody()));
    }

    private static void assertMatchesGolden(String name, ResponseEntity<?> response) throws IOException {
        JsonNode actual = tree(response);
        Path golden = GOLDEN_DIR.resolve(name);
        if (Boolean.parseBoolean(System.getenv("METHODOLOGY_GOLDEN_UPDATE"))) {
            Files.writeString(golden, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(actual) + "\n");
        }
        assertThat(actual)
                .as("%s drifted from its golden; if the change is deliberate, regenerate it with METHODOLOGY_GOLDEN_UPDATE=true", name)
                .isEqualTo(MAPPER.readTree(golden.toFile()));
    }
}
