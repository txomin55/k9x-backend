package com.k9x.configuration.extractions;

import com.k9x.application.extractions.use_case.GetExtractionLogServiceCase;
import com.k9x.infrastructure.in.rest.endpoints.extractions.FetchExtractionLog;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExtractionsEndpointConfiguration {

    @Bean
    public FetchExtractionLog fetchExtractionLog(GetExtractionLogServiceCase getExtractionLogServiceCase,
                                                 ReferenceNameResolver referenceNameResolver) {
        return new FetchExtractionLog(getExtractionLogServiceCase, referenceNameResolver);
    }
}
