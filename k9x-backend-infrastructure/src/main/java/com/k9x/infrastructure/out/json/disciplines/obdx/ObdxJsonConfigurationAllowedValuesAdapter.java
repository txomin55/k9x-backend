package com.k9x.infrastructure.out.json.disciplines.obdx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.disciplines.obdx.exceptions.ExerciseConfigurationNotFoundException;
import com.k9x.application.disciplines.obdx.port.GetObdxConfigurationAllowedValuesPort;
import com.k9x.infrastructure.out.json.disciplines.obdx.dto.ExerciseAllowedValuesConfigurationDTO;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObdxJsonConfigurationAllowedValuesAdapter implements GetObdxConfigurationAllowedValuesPort {

    private static final String PATTERN = "classpath:com/k9x/infrastructure/out/json/disciplines/obdx/federations/**/configuration.json";

    private final Map<String, List<BigDecimal>> allowedValuesByConfigId;

    public ObdxJsonConfigurationAllowedValuesAdapter(ObjectMapper objectMapper) {
        this.allowedValuesByConfigId = buildIndex(objectMapper);
    }

    @Override
    public List<BigDecimal> getAllowedValues(String configurationId) {
        List<BigDecimal> values = allowedValuesByConfigId.get(configurationId);
        if (values == null) throw new ExerciseConfigurationNotFoundException();
        return values;
    }

    private static Map<String, List<BigDecimal>> buildIndex(ObjectMapper objectMapper) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Map<String, List<BigDecimal>> result = new HashMap<>();
        try {
            for (Resource resource : resolver.getResources(PATTERN)) {
                ExerciseAllowedValuesConfigurationDTO json;
                try (var is = resource.getInputStream()) {
                    json = objectMapper.readValue(is, ExerciseAllowedValuesConfigurationDTO.class);
                }
                if (json.allowedValues() != null) {
                    result.put(json.id(), json.allowedValues());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load OBDX configuration allowed values", e);
        }
        return result;
    }
}
