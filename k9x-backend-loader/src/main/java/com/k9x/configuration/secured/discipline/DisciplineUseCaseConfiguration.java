package com.k9x.configuration.secured.discipline;

import com.k9x.application.awards.port.GetAwardListPort;
import com.k9x.application.awards.use_case.GetAwardListServiceCase;
import com.k9x.application.categories.port.GetEventCategoryListPort;
import com.k9x.application.categories.use_case.GetEventCategoryListServiceCase;
import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.GetDisciplineFederationsConfigurationsServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DisciplineUseCaseConfiguration {

    @Bean
    public GetDisciplineFederationsConfigurationsServiceCase getDisciplineFederationsConfigurationsServiceCase(
            GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort) {
        return new GetDisciplineFederationsConfigurationsServiceCase(getObdxFederationsConfigurationsPort);
    }

    @Bean
    public GetAwardListServiceCase getAwardListServiceCase(GetAwardListPort getAwardListPort) {
        return new GetAwardListServiceCase(getAwardListPort);
    }

    @Bean
    public GetEventCategoryListServiceCase getEventCategoryListServiceCase(
            GetEventCategoryListPort getEventCategoryListPort) {
        return new GetEventCategoryListServiceCase(getEventCategoryListPort);
    }
}
