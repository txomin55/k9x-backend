package com.k9x.configuration.secured.judges;

import com.k9x.application.judges.use_case.GetJudgeListServiceCase;
import com.k9x.application.users.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.endpoints.secured.judges.CreateJudge;
import com.k9x.infrastructure.in.rest.endpoints.secured.judges.FetchJudges;
import com.k9x.infrastructure.in.rest.endpoints.secured.judges.RemoveJudge;
import com.k9x.infrastructure.in.rest.endpoints.secured.judges.UpdateJudge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredJudgesEndpointConfiguration {

    @Bean
    public CreateJudge createJudge() {
        return new CreateJudge();
    }

    @Bean
    public FetchJudges fetchJudges(GetJudgeListServiceCase getJudgeListServiceCase, UserInfoDTO userInfoDTO) {
        return new FetchJudges(getJudgeListServiceCase, userInfoDTO);
    }

    @Bean
    public RemoveJudge removeJudge() {
        return new RemoveJudge();
    }

    @Bean
    public UpdateJudge updateJudge() {
        return new UpdateJudge();
    }
}
