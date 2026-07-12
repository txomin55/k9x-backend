package com.k9x.infrastructure.out.json.disciplines.obdx;

import com.k9x.application.disciplines.obdx.exceptions.ExerciseConfigurationNotFoundException;
import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.use_case.dto.ObdxClassificationConfigDTO;
import com.k9x.domain.disciplines.valueobjects.ClassificationCacheEvictStrategy;
import com.k9x.infrastructure.out.json.disciplines.obdx.dto.FederationConfigurationFileDTO;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObdxJsonClassificationConfigAdapter implements GetObdxClassificationConfigPort {

    private final Map<String, ObdxClassificationConfigDTO> configById;

    public ObdxJsonClassificationConfigAdapter(ObdxFederationsConfigurationsCache cache) {
        this.configById = buildIndex(cache);
    }

    private static Map<String, ObdxClassificationConfigDTO> buildIndex(ObdxFederationsConfigurationsCache cache) {
        Map<String, ObdxClassificationConfigDTO> result = new HashMap<>();
        for (ObdxFederationsConfigurationsCache.Entry entry : cache.getAll()) {
            FederationConfigurationFileDTO raw = entry.configuration();
            Map<String, BigDecimal> coefs = new HashMap<>();
            if (raw.exercises() != null) {
                for (FederationConfigurationFileDTO.Exercise ex : raw.exercises()) {
                    if (ex.coef() != null) coefs.put(ex.id(), ex.coef());
                }
            }
            BigDecimal maxAllowedScore = raw.allowedValues() != null
                    ? raw.allowedValues().stream().max(Comparator.naturalOrder()).orElse(BigDecimal.TEN)
                    : BigDecimal.TEN;
            List<ObdxClassificationConfigDTO.QualificationThreshold> qualifications = raw.qualifications() == null
                    ? List.of()
                    : raw.qualifications().stream()
                    .filter(q -> q.id() != null && q.minScore() != null)
                    .map(q -> new ObdxClassificationConfigDTO.QualificationThreshold(q.id(), q.minScore()))
                    .toList();
            result.put(raw.id(), new ObdxClassificationConfigDTO(
                    ClassificationCacheEvictStrategy.OBDX,
                    maxAllowedScore,
                    coefs,
                    raw.breakTie() != null ? raw.breakTie() : List.of(),
                    raw.breakTieTie() != null ? raw.breakTieTie() : List.of(),
                    qualifications));
        }
        return result;
    }

    @Override
    public ObdxClassificationConfigDTO getConfig(String configurationId) {
        ObdxClassificationConfigDTO config = configById.get(configurationId);
        if (config == null) throw new ExerciseConfigurationNotFoundException();
        return config;
    }
}
