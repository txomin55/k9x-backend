package com.k9x.configuration.methodology;

import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.methodology.port.GetMethodologyCatalogPort;
import com.k9x.application.methodology.use_case.GetK9xMethodologyServiceCase;
import com.k9x.application.methodology.use_case.GetObdxMethodologyServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MethodologyUseCaseConfiguration {

    @Bean
    public GetObdxMethodologyServiceCase getObdxMethodologyServiceCase(
            GetMethodologyCatalogPort getMethodologyCatalogPort,
            GetObdxClassificationConfigPort getObdxClassificationConfigPort) {
        return new GetObdxMethodologyServiceCase(getMethodologyCatalogPort, getObdxClassificationConfigPort);
    }

    @Bean
    public GetK9xMethodologyServiceCase getK9xMethodologyServiceCase(
            GetMethodologyCatalogPort getMethodologyCatalogPort,
            GetObdxClassificationConfigPort getObdxClassificationConfigPort) {
        return new GetK9xMethodologyServiceCase(getMethodologyCatalogPort, getObdxClassificationConfigPort);
    }
}
