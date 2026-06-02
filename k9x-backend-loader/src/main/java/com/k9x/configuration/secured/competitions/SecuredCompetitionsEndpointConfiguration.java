package com.k9x.configuration.secured.competitions;

import com.k9x.application.competitions.use_case.CreateCompetitionServiceCase;
import com.k9x.application.competitions.use_case.DeleteCompetitionServiceCase;
import com.k9x.application.competitions.use_case.GetCompetitionListServiceCase;
import com.k9x.application.competitions.use_case.UpdateCompetitionServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.endpoints.secured.competitions.CreateCompetition;
import com.k9x.infrastructure.in.rest.endpoints.secured.competitions.FetchCompetitions;
import com.k9x.infrastructure.in.rest.endpoints.secured.competitions.RemoveCompetition;
import com.k9x.infrastructure.in.rest.endpoints.secured.competitions.UpdateCompetition;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredCompetitionsEndpointConfiguration {

    @Bean
    public CreateCompetition createCompetition(CreateCompetitionServiceCase createCompetitionServiceCase, UserInfoDTO userInfoDTO) {
        return new CreateCompetition(createCompetitionServiceCase, userInfoDTO);
    }

    @Bean
    public FetchCompetitions fetchCompetitions(GetCompetitionListServiceCase getCompetitionListServiceCase, UserInfoDTO userInfoDTO,
                                               MessageSource messageSource) {
        return new FetchCompetitions(getCompetitionListServiceCase, userInfoDTO, messageSource);
    }

    @Bean
    public RemoveCompetition removeCompetition(DeleteCompetitionServiceCase deleteCompetitionServiceCase, UserInfoDTO userInfoDTO) {
        return new RemoveCompetition(deleteCompetitionServiceCase, userInfoDTO);
    }

    @Bean
    public UpdateCompetition updateCompetition(UpdateCompetitionServiceCase updateCompetitionServiceCase, UserInfoDTO userInfoDTO) {
        return new UpdateCompetition(updateCompetitionServiceCase, userInfoDTO);
    }
}
