package com.k9x.infrastructure.out.json.disciplines.obdx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.infrastructure.out.json.disciplines.obdx.dto.FederationConfigurationFileDTO;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class ObdxFederationsConfigurationsCache {

    private static final String PATTERN = "classpath:com/k9x/infrastructure/out/json/disciplines/obdx/federations/**/configuration.json";
    private static final String FEDERATIONS_PREFIX = "federations/";

    /**
     * Una configuración por fichero: {@code federations/<federación>/<clase>/v<año>/configuration.json}.
     * El año es el del reglamento publicado, y es lo que ordena una versión frente a la siguiente.
     */
    public record Entry(String federationKey, String classKey, int version, FederationConfigurationFileDTO configuration) {}

    private final List<Entry> entries;

    public ObdxFederationsConfigurationsCache(ObjectMapper objectMapper) {
        this.entries = load(objectMapper);
    }

    ObdxFederationsConfigurationsCache(List<Entry> entries) {
        this.entries = List.copyOf(entries);
    }

    public List<Entry> getAll() {
        return entries;
    }

    /**
     * De cada clase de cada federación conviven varias versiones —una por reglamento publicado—, pero solo una
     * está vigente: la del año más alto. Las anteriores siguen en {@link #getAll()} porque los eventos ya
     * puntuados las referencian por id.
     */
    public List<Entry> getCurrent() {
        LinkedHashMap<String, Entry> current = new LinkedHashMap<>();
        for (Entry entry : entries) {
            current.merge(entry.federationKey() + "/" + entry.classKey(), entry,
                    (previous, candidate) -> candidate.version() > previous.version() ? candidate : previous);
        }
        return List.copyOf(current.values());
    }

    private static List<Entry> load(ObjectMapper objectMapper) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            return Arrays.stream(resolver.getResources(PATTERN))
                    .map(resource -> toEntry(resource, objectMapper))
                    .sorted(Comparator.comparing(Entry::federationKey)
                            .thenComparing(Entry::classKey)
                            .thenComparingInt(Entry::version))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load OBDX federation configurations", e);
        }
    }

    private static Entry toEntry(Resource resource, ObjectMapper objectMapper) {
        String[] path = federationPathSegments(resource);
        return new Entry(path[0], path[1], parseVersion(path[2]), parseJson(resource, objectMapper));
    }

    private static String[] federationPathSegments(Resource resource) {
        try {
            String path = resource.getURL().getPath();
            int idx = path.indexOf(FEDERATIONS_PREFIX);
            return path.substring(idx + FEDERATIONS_PREFIX.length()).split("/");
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract federation key from resource path", e);
        }
    }

    private static int parseVersion(String directory) {
        try {
            return Integer.parseInt(directory.startsWith("v") ? directory.substring(1) : directory);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Unexpected OBDX configuration version directory: " + directory, e);
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
