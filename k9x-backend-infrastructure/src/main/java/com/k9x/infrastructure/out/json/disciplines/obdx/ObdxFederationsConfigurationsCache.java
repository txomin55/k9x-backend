package com.k9x.infrastructure.out.json.disciplines.obdx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.infrastructure.out.json.disciplines.obdx.dto.FederationConfigurationFileDTO;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ObdxFederationsConfigurationsCache {

    private static final String PATTERN = "classpath:com/k9x/infrastructure/out/json/disciplines/obdx/federations/**/configuration.json";
    private static final String FEDERATIONS_PREFIX = "federations/";

    public record Entry(String federationKey, FederationConfigurationFileDTO configuration) {}

    private final List<Entry> entries;

    public ObdxFederationsConfigurationsCache(ObjectMapper objectMapper) {
        this.entries = load(objectMapper);
    }

    public List<Entry> getAll() {
        return entries;
    }

    private static List<Entry> load(ObjectMapper objectMapper) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            return Arrays.stream(resolver.getResources(PATTERN))
                    .sorted(Comparator.comparing(r -> r.getFilename() != null ? r.getFilename() : ""))
                    .map(resource -> new Entry(extractFederationKey(resource), parseJson(resource, objectMapper)))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load OBDX federation configurations", e);
        }
    }

    private static String extractFederationKey(Resource resource) {
        try {
            String path = resource.getURL().getPath();
            int idx = path.indexOf(FEDERATIONS_PREFIX);
            String afterFederations = path.substring(idx + FEDERATIONS_PREFIX.length());
            return afterFederations.substring(0, afterFederations.indexOf('/'));
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract federation key from resource path", e);
        }
    }

    private static FederationConfigurationFileDTO parseJson(Resource resource, ObjectMapper objectMapper) {
        try (var is = resource.getInputStream()) {
            return objectMapper.readValue(is, FederationConfigurationFileDTO.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse federation configuration: " + resource.getFilename(), e);
        }
    }
}
