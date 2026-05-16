package com.k9x.configuration.secured.judges;

import com.k9x.application.judges.port.GetJudgeListPersistencePort;
import com.k9x.application.judges.use_case.GetJudgeListServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredJudgeUseCaseConfiguration {

    @Bean
    public GetJudgeListServiceCase getJudgeListServiceCase(GetJudgeListPersistencePort getJudgeListPersistencePort) {
        return new GetJudgeListServiceCase(getJudgeListPersistencePort);
    }
}
