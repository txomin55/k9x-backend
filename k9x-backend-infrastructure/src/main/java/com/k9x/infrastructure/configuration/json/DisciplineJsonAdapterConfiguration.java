package com.k9x.infrastructure.configuration.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.disciplines.obdx.port.GetObdxConfigurationAllowedValuesPort;
import com.k9x.application.disciplines.obdx.port.GetObdxExerciseAllowedValuesPort;
import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.infrastructure.out.json.disciplines.obdx.ObdxFederationsConfigurationsCache;
import com.k9x.infrastructure.out.json.disciplines.obdx.ObdxJsonConfigurationAllowedValuesAdapter;
import com.k9x.infrastructure.out.json.disciplines.obdx.ObdxJsonExerciseAllowedValuesAdapter;
import com.k9x.infrastructure.out.json.disciplines.obdx.ObdxJsonFederationsConfigurationsAdapter;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DisciplineJsonAdapterConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ObdxFederationsConfigurationsCache obdxFederationsConfigurationsCache(ObjectMapper objectMapper) {
        return new ObdxFederationsConfigurationsCache(objectMapper);
    }

    @Bean
    public GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort(
            ObdxFederationsConfigurationsCache cache, MessageSource messageSource) {
        return new ObdxJsonFederationsConfigurationsAdapter(cache, messageSource);
    }

    @Bean
    public GetObdxExerciseAllowedValuesPort getObdxExerciseAllowedValuesPort(
            ObdxFederationsConfigurationsCache cache) {
        return new ObdxJsonExerciseAllowedValuesAdapter(cache);
    }

    @Bean
    public GetObdxConfigurationAllowedValuesPort getObdxConfigurationAllowedValuesPort(
            ObdxFederationsConfigurationsCache cache) {
        return new ObdxJsonConfigurationAllowedValuesAdapter(cache);
    }
}
