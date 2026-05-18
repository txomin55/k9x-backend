package com.k9x.configuration.secured.stages;

import com.k9x.application.stages.use_case.CreateStageServiceCase;
import com.k9x.application.stages.use_case.DeleteStageServiceCase;
import com.k9x.application.stages.use_case.UpdateStageServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.endpoints.secured.stages.CreateStage;
import com.k9x.infrastructure.in.rest.endpoints.secured.stages.RemoveStage;
import com.k9x.infrastructure.in.rest.endpoints.secured.stages.UpdateStage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredStagesEndpointConfiguration {

    @Bean
    public CreateStage createStage(CreateStageServiceCase createStageServiceCase, UserInfoDTO userInfoDTO) {
        return new CreateStage(createStageServiceCase, userInfoDTO);
    }

    @Bean
    public RemoveStage removeStage(DeleteStageServiceCase deleteStageServiceCase, UserInfoDTO userInfoDTO) {
        return new RemoveStage(deleteStageServiceCase, userInfoDTO);
    }

    @Bean
    public UpdateStage updateStage(UpdateStageServiceCase updateStageServiceCase, UserInfoDTO userInfoDTO) {
        return new UpdateStage(updateStageServiceCase, userInfoDTO);
    }
}
