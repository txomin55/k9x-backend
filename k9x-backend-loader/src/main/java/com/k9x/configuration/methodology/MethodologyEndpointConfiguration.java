package com.k9x.configuration.methodology;

import com.k9x.application.methodology.use_case.GetK9xMethodologyServiceCase;
import com.k9x.application.methodology.use_case.GetObdxMethodologyServiceCase;
import com.k9x.infrastructure.in.rest.endpoints.methodology.FetchK9xMethodology;
import com.k9x.infrastructure.in.rest.endpoints.methodology.FetchObdxMethodology;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MethodologyEndpointConfiguration {

    @Bean
    public FetchObdxMethodology fetchObdxMethodology(GetObdxMethodologyServiceCase getObdxMethodologyServiceCase) {
        return new FetchObdxMethodology(getObdxMethodologyServiceCase);
    }

    @Bean
    public FetchK9xMethodology fetchK9xMethodology(GetK9xMethodologyServiceCase getK9xMethodologyServiceCase) {
        return new FetchK9xMethodology(getK9xMethodologyServiceCase);
    }
}
