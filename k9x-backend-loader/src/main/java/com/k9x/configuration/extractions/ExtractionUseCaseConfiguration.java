package com.k9x.configuration.extractions;

import com.k9x.application.extractions.port.GetExtractedCompetitionsPersistencePort;
import com.k9x.application.extractions.use_case.GetExtractionLogServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExtractionUseCaseConfiguration {

    @Bean
    public GetExtractionLogServiceCase getExtractionLogServiceCase(
            GetExtractedCompetitionsPersistencePort getExtractedCompetitionsPersistencePort) {
        return new GetExtractionLogServiceCase(getExtractedCompetitionsPersistencePort);
    }
}
