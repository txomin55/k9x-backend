package com.k9x.configuration.secured.dogs;

import com.k9x.application.dogs.use_case.CreateDogServiceCase;
import com.k9x.application.dogs.use_case.DeleteDogServiceCase;
import com.k9x.application.dogs.use_case.GetDogListServiceCase;
import com.k9x.application.dogs.use_case.UpdateDogServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.endpoints.secured.dogs.CreateDog;
import com.k9x.infrastructure.in.rest.endpoints.secured.dogs.GetDogList;
import com.k9x.infrastructure.in.rest.endpoints.secured.dogs.RemoveDog;
import com.k9x.infrastructure.in.rest.endpoints.secured.dogs.UpdateDog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredDogsEndpointConfiguration {

    @Bean
    public CreateDog createDog(CreateDogServiceCase createDogServiceCase, UserInfoDTO userInfoDTO) {
        return new CreateDog(createDogServiceCase, userInfoDTO);
    }

    @Bean
    public RemoveDog removeDog(DeleteDogServiceCase deleteDogServiceCase, UserInfoDTO userInfoDTO) {
        return new RemoveDog(deleteDogServiceCase, userInfoDTO);
    }

    @Bean
    public UpdateDog updateDog(UpdateDogServiceCase updateDogServiceCase, UserInfoDTO userInfoDTO) {
        return new UpdateDog(updateDogServiceCase, userInfoDTO);
    }

    @Bean
    public GetDogList getDogList(GetDogListServiceCase getDogListServiceCase, UserInfoDTO userInfoDTO) {
        return new GetDogList(getDogListServiceCase, userInfoDTO);
    }
}
