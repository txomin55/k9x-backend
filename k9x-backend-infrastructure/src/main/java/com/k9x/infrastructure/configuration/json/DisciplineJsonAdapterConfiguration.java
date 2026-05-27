package com.k9x.infrastructure.configuration.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.disciplines.obdx.port.GetObdxExerciseAllowedValuesPort;
import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.infrastructure.out.json.disciplines.obdx.ObdxJsonExerciseAllowedValuesAdapter;
import com.k9x.infrastructure.out.json.disciplines.obdx.ObdxJsonFederationsConfigurationsAdapter;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DisciplineJsonAdapterConfiguration {

    @Bean
    public GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort(
            ObjectMapper objectMapper, MessageSource messageSource) {
        return new ObdxJsonFederationsConfigurationsAdapter(objectMapper, messageSource);
    }

    @Bean
    public GetObdxExerciseAllowedValuesPort getObdxExerciseAllowedValuesPort(ObjectMapper objectMapper) {
        return new ObdxJsonExerciseAllowedValuesAdapter(objectMapper);
    }
}
