package com.k9x.configuration.secured.stages;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.stages.port.CreateStagePersistencePort;
import com.k9x.application.stages.port.DeleteStagePersistencePort;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.application.stages.port.UpdateStagePersistencePort;
import com.k9x.application.stages.use_case.CreateStageServiceCase;
import com.k9x.application.stages.use_case.DeleteStageServiceCase;
import com.k9x.application.stages.use_case.UpdateStageServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredStageUseCaseConfiguration {

    @Bean
    public CreateStageServiceCase createStageServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                         CreateStagePersistencePort createStagePersistencePort) {
        return new CreateStageServiceCase(getCompetitionPersistencePort, createStagePersistencePort);
    }

    @Bean
    public UpdateStageServiceCase updateStageServiceCase(GetStagePersistencePort getStagePersistencePort,
                                                         GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                         UpdateStagePersistencePort updateStagePersistencePort) {
        return new UpdateStageServiceCase(getStagePersistencePort, getCompetitionPersistencePort, updateStagePersistencePort);
    }

    @Bean
    public DeleteStageServiceCase deleteStageServiceCase(GetStagePersistencePort getStagePersistencePort,
                                                         GetCompetitionPersistencePort getCompetitionPersistencePort,
                                                         DeleteStagePersistencePort deleteStagePersistencePort) {
        return new DeleteStageServiceCase(getStagePersistencePort, getCompetitionPersistencePort, deleteStagePersistencePort);
    }
}
