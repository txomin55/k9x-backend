package com.k9x.configuration.secured.competitions;

import com.k9x.application.competitions.use_case.CreateCompetitionServiceCase;
import com.k9x.application.competitions.use_case.UpdateCompetitionServiceCase;
import com.k9x.application.users.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.endpoints.secured.competitions.CreateCompetition;
import com.k9x.infrastructure.in.rest.endpoints.secured.competitions.FetchCompetitions;
import com.k9x.infrastructure.in.rest.endpoints.secured.competitions.RemoveCompetition;
import com.k9x.infrastructure.in.rest.endpoints.secured.competitions.UpdateCompetition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredCompetitionsEndpointConfiguration {

    @Bean
    public CreateCompetition createCompetition(CreateCompetitionServiceCase createCompetitionServiceCase, UserInfoDTO userInfoDTO) {
        return new CreateCompetition(createCompetitionServiceCase, userInfoDTO);
    }

    @Bean
    public FetchCompetitions fetchCompetitions() {
        return new FetchCompetitions();
    }

    @Bean
    public RemoveCompetition removeCompetition() {
        return new RemoveCompetition();
    }

    @Bean
    public UpdateCompetition updateCompetition(UpdateCompetitionServiceCase updateCompetitionServiceCase, UserInfoDTO userInfoDTO) {
        return new UpdateCompetition(updateCompetitionServiceCase, userInfoDTO);
    }
}
