package com.k9x.configuration.secured.discipline;

import com.k9x.application.disciplines.obdx.use_case.GetObdxFederationsConfigurationsServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.endpoints.secured.disciplines.obdx.GetObdxFederationsConfigurations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredDisciplineEndpointConfiguration {

    @Bean
    public GetObdxFederationsConfigurations getDisciplines(
            GetObdxFederationsConfigurationsServiceCase getObdxFederationsConfigurationsServiceCase,
            UserInfoDTO userInfoDTO) {
        return new GetObdxFederationsConfigurations(getObdxFederationsConfigurationsServiceCase, userInfoDTO);
    }
}
