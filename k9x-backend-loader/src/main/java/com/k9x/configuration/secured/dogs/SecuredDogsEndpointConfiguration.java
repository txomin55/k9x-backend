package com.k9x.configuration.secured.dogs;

import com.k9x.application.dogs.use_case.GetDogListServiceCase;
import com.k9x.application.users.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.endpoints.secured.dogs.CreateDog;
import com.k9x.infrastructure.in.rest.endpoints.secured.dogs.GetDogList;
import com.k9x.infrastructure.in.rest.endpoints.secured.dogs.RemoveDog;
import com.k9x.infrastructure.in.rest.endpoints.secured.dogs.UpdateDog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredDogsEndpointConfiguration {

    @Bean
    public CreateDog createDog() {
        return new CreateDog();
    }

    @Bean
    public RemoveDog removeDog() {
        return new RemoveDog();
    }

    @Bean
    public UpdateDog updateDog() {
        return new UpdateDog();
    }

    @Bean
    public GetDogList getDogList(GetDogListServiceCase getDogListServiceCase, UserInfoDTO userInfoDTO) {
        return new GetDogList(getDogListServiceCase, userInfoDTO);
    }
}
