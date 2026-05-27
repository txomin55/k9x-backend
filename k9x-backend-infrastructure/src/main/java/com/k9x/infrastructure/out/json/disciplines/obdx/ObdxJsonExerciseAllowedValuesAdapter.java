package com.k9x.infrastructure.out.json.disciplines.obdx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.disciplines.obdx.exceptions.ExerciseConfigurationNotFoundException;
import com.k9x.application.disciplines.obdx.port.GetObdxExerciseAllowedValuesPort;
import com.k9x.infrastructure.out.json.disciplines.obdx.dto.ExerciseAllowedValuesConfigurationDTO;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObdxJsonExerciseAllowedValuesAdapter implements GetObdxExerciseAllowedValuesPort {

    private static final String PATTERN = "classpath:com/k9x/infrastructure/out/json/disciplines/obdx/federations/**/configuration.json";

    private final Map<String, List<BigDecimal>> allowedValuesByExerciseId;

    public ObdxJsonExerciseAllowedValuesAdapter(ObjectMapper objectMapper) {
        this.allowedValuesByExerciseId = buildIndex(objectMapper);
    }

    @Override
    public List<BigDecimal> getAllowedValues(String exerciseId) {
        List<BigDecimal> values = allowedValuesByExerciseId.get(exerciseId);
        if (values == null) throw new ExerciseConfigurationNotFoundException();
        return values;
    }

    private static Map<String, List<BigDecimal>> buildIndex(ObjectMapper objectMapper) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Map<String, List<BigDecimal>> result = new HashMap<>();
        try {
            Resource[] resources = resolver.getResources(PATTERN);
            for (Resource resource : resources) {
                ExerciseAllowedValuesConfigurationDTO json;
                try (var is = resource.getInputStream()) {
                    json = objectMapper.readValue(is, ExerciseAllowedValuesConfigurationDTO.class);
                }
                List<BigDecimal> allowedValues = json.allowedValues();
                if (allowedValues == null) continue;
                for (ExerciseAllowedValuesConfigurationDTO.Exercise exercise : json.exercises()) {
                    result.put(exercise.id(), allowedValues);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load OBDX exercise allowed values", e);
        }
        return result;
    }
}
