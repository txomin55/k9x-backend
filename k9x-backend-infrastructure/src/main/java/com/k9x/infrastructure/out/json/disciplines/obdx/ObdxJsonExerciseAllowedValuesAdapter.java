package com.k9x.infrastructure.out.json.disciplines.obdx;

import com.k9x.application.disciplines.obdx.exceptions.ExerciseConfigurationNotFoundException;
import com.k9x.application.disciplines.obdx.port.GetObdxExerciseAllowedValuesPort;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObdxJsonExerciseAllowedValuesAdapter implements GetObdxExerciseAllowedValuesPort {

    private final Map<String, List<BigDecimal>> allowedValuesByExerciseId;

    public ObdxJsonExerciseAllowedValuesAdapter(ObdxFederationsConfigurationsCache cache) {
        this.allowedValuesByExerciseId = buildIndex(cache);
    }

    private static Map<String, List<BigDecimal>> buildIndex(ObdxFederationsConfigurationsCache cache) {
        Map<String, List<BigDecimal>> result = new HashMap<>();
        for (ObdxFederationsConfigurationsCache.Entry entry : cache.getAll()) {
            List<BigDecimal> allowedValues = entry.configuration().allowedValues();
            if (allowedValues == null) continue;
            for (var exercise : entry.configuration().exercises()) {
                result.put(exercise.id(), allowedValues);
            }
        }
        return result;
    }

    @Override
    public List<BigDecimal> getAllowedValues(String exerciseId) {
        List<BigDecimal> values = allowedValuesByExerciseId.get(exerciseId);
        if (values == null) throw new ExerciseConfigurationNotFoundException();
        return values;
    }
}
