package com.k9x.infrastructure.out.json.disciplines.obdx;

import com.k9x.application.disciplines.obdx.exceptions.ExerciseConfigurationNotFoundException;
import com.k9x.application.disciplines.obdx.port.GetObdxConfigurationAllowedValuesPort;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObdxJsonConfigurationAllowedValuesAdapter implements GetObdxConfigurationAllowedValuesPort {

    private final Map<String, List<BigDecimal>> allowedValuesByConfigId;

    public ObdxJsonConfigurationAllowedValuesAdapter(ObdxFederationsConfigurationsCache cache) {
        this.allowedValuesByConfigId = buildIndex(cache);
    }

    @Override
    public List<BigDecimal> getAllowedValues(String configurationId) {
        List<BigDecimal> values = allowedValuesByConfigId.get(configurationId);
        if (values == null) throw new ExerciseConfigurationNotFoundException();
        return values;
    }

    private static Map<String, List<BigDecimal>> buildIndex(ObdxFederationsConfigurationsCache cache) {
        Map<String, List<BigDecimal>> result = new HashMap<>();
        for (ObdxFederationsConfigurationsCache.Entry entry : cache.getAll()) {
            if (entry.configuration().allowedValues() != null) {
                result.put(entry.configuration().id(), entry.configuration().allowedValues());
            }
        }
        return result;
    }
}
