package com.k9x.configuration.secured.judges;

import com.k9x.application.judges.port.DeleteJudgePersistencePort;
import com.k9x.application.judges.port.GetJudgeListPersistencePort;
import com.k9x.application.judges.port.GetJudgePersistencePort;
import com.k9x.application.judges.use_case.DeleteJudgeServiceCase;
import com.k9x.application.judges.use_case.GetJudgeListServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredJudgeUseCaseConfiguration {

    @Bean
    public GetJudgeListServiceCase getJudgeListServiceCase(GetJudgeListPersistencePort getJudgeListPersistencePort) {
        return new GetJudgeListServiceCase(getJudgeListPersistencePort);
    }

    @Bean
    public DeleteJudgeServiceCase deleteJudgeServiceCase(GetJudgePersistencePort getJudgePersistencePort,
                                                         DeleteJudgePersistencePort deleteJudgePersistencePort) {
        return new DeleteJudgeServiceCase(getJudgePersistencePort, deleteJudgePersistencePort);
    }
}
