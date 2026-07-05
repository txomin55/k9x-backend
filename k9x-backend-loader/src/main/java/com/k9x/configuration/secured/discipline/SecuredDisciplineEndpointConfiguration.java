package com.k9x.configuration.secured.discipline;

import com.k9x.application.awards.use_case.GetAwardListServiceCase;
import com.k9x.application.disciplines.use_case.GetDisciplineFederationsConfigurationsServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.endpoints.secured.disciplines.FetchDisciplineAwards;
import com.k9x.infrastructure.in.rest.endpoints.secured.disciplines.GetFederationsConfigurations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredDisciplineEndpointConfiguration {

    @Bean
    public GetFederationsConfigurations getDisciplines(
            GetDisciplineFederationsConfigurationsServiceCase getDisciplineFederationsConfigurationsServiceCase,
            UserInfoDTO userInfoDTO) {
        return new GetFederationsConfigurations(getDisciplineFederationsConfigurationsServiceCase, userInfoDTO);
    }

    @Bean
    public FetchDisciplineAwards fetchDisciplineAwards(GetAwardListServiceCase getAwardListServiceCase) {
        return new FetchDisciplineAwards(getAwardListServiceCase);
    }
}
