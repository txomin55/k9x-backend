package com.k9x.infrastructure.out.json.disciplines.obdx;

import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.obdx.use_case.dto.ExerciseDTO;
import com.k9x.application.disciplines.obdx.use_case.dto.ObdxConfigurationDTO;
import com.k9x.application.disciplines.obdx.use_case.dto.ObdxConfigurationsDTO;
import com.k9x.application.disciplines.obdx.use_case.dto.ObdxFederationInfoDTO;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ObdxJsonFederationsConfigurationsAdapter implements GetObdxFederationsConfigurationsPort {

    private final ObdxFederationsConfigurationsCache cache;
    private final MessageSource messageSource;

    public ObdxJsonFederationsConfigurationsAdapter(ObdxFederationsConfigurationsCache cache, MessageSource messageSource) {
        this.cache = cache;
        this.messageSource = messageSource;
    }

    @Override
    public List<ObdxConfigurationsDTO> getConfigurations() {
        LinkedHashMap<String, List<ObdxConfigurationDTO>> byFederation = new LinkedHashMap<>();
        LinkedHashMap<String, String> countryByFederation = new LinkedHashMap<>();

        for (ObdxFederationsConfigurationsCache.Entry entry : cache.getAll()) {
            String federationKey = entry.federationKey();
            var config = entry.configuration();
            countryByFederation.putIfAbsent(federationKey, config.country());
            List<ExerciseDTO> exercises = config.exercises().stream()
                    .map(e -> new ExerciseDTO(e.id(), translate(e.id())))
                    .toList();
            byFederation.computeIfAbsent(federationKey, _ -> new ArrayList<>())
                    .add(new ObdxConfigurationDTO(config.id(), translate(config.id()), exercises));
        }

        return byFederation.entrySet().stream()
                .map(entry -> new ObdxConfigurationsDTO(
                        federationInfo(entry.getKey(), countryByFederation.get(entry.getKey())),
                        entry.getValue()))
                .toList();
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
