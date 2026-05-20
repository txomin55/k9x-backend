package com.k9x.infrastructure.out.json.disciplines.obdx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.obdx.use_case.dto.ExerciseDTO;
import com.k9x.application.disciplines.obdx.use_case.dto.ObdxConfigurationDTO;
import com.k9x.application.disciplines.obdx.use_case.dto.ObdxConfigurationsDTO;
import com.k9x.application.disciplines.obdx.use_case.dto.ObdxFederationInfoDTO;
import com.k9x.infrastructure.out.json.disciplines.obdx.dto.SimplifiedFederationConfigurationDTO;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.*;

public class ObdxJsonFederationsConfigurationsAdapter implements GetObdxFederationsConfigurationsPort {

    private static final String PATTERN = "classpath:com/k9x/infrastructure/out/json/disciplines/obdx/federations/**/configuration.json";
    private static final String FEDERATIONS_PREFIX = "federations/";

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    public ObdxJsonFederationsConfigurationsAdapter(ObjectMapper objectMapper, MessageSource messageSource) {
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
    }

    @Override
    public List<ObdxConfigurationsDTO> getConfigurations() throws IOException {
        LinkedHashMap<String, List<ObdxConfigurationDTO>> byFederation = new LinkedHashMap<>();
        LinkedHashMap<String, String> countryByFederation = new LinkedHashMap<>();
        Resource[] resources = resolver.getResources(PATTERN);
        Arrays.stream(resources)
                .sorted(Comparator.comparing(r -> r.getFilename() != null ? r.getFilename() : ""))
                .forEach(resource -> {
                    String federationKey = extractFederationKey(resource);
                    SimplifiedFederationConfigurationDTO json = parseJson(resource);
                    countryByFederation.putIfAbsent(federationKey, json.country());
                    List<ExerciseDTO> exercises = json.exercises().stream()
                            .map(e -> new ExerciseDTO(e.id(), translate(e.id())))
                            .toList();
                    byFederation.computeIfAbsent(federationKey, _ -> new ArrayList<>())
                            .add(new ObdxConfigurationDTO(json.id(), translate(json.id()), exercises));
                });

        return byFederation.entrySet().stream()
                .map(entry -> new ObdxConfigurationsDTO(
                        federationInfo(entry.getKey(), countryByFederation.get(entry.getKey())),
                        entry.getValue()))
                .toList();
    }

    private String extractFederationKey(Resource resource) {
        try {
            String path = resource.getURL().getPath();
            int idx = path.indexOf(FEDERATIONS_PREFIX);
            String afterFederations = path.substring(idx + FEDERATIONS_PREFIX.length());
            return afterFederations.substring(0, afterFederations.indexOf('/'));
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract federation from resource path", e);
        }
    }

    private SimplifiedFederationConfigurationDTO parseJson(Resource resource) {
        try (var is = resource.getInputStream()) {
            return objectMapper.readValue(is, SimplifiedFederationConfigurationDTO.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load discipline configuration: " + resource.getFilename(), e);
        }
    }

    private ObdxFederationInfoDTO federationInfo(String key, String country) {
        return new ObdxFederationInfoDTO(
                key.toUpperCase(),
                translate("federation." + key + ".name", key),
                country);
    }

    private String translate(String id) {
        return messageSource.getMessage(id, null, id, LocaleContextHolder.getLocale());
    }

    private String translate(String id, String defaultValue) {
        return messageSource.getMessage(id, null, defaultValue, LocaleContextHolder.getLocale());
    }

}
