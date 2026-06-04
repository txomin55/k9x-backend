package com.k9x.configuration.secured.collections;

import com.k9x.application.collections.use_case.GetCollectionListServiceCase;
import com.k9x.application.collections.use_case.GetCollectionServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.endpoints.secured.collections.GetCollection;
import com.k9x.infrastructure.in.rest.endpoints.secured.collections.GetCollections;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredCollectionsEndpointConfiguration {

    @Bean
    public GetCollection getObdxCollection(GetCollectionServiceCase getCollectionServiceCase, UserInfoDTO userInfoDTO,
                                           MessageSource messageSource) {
        return new GetCollection(getCollectionServiceCase, userInfoDTO, messageSource);
    }

    @Bean
    public GetCollections getCollections(GetCollectionListServiceCase getCollectionListServiceCase, UserInfoDTO userInfoDTO,
                                         MessageSource messageSource) {
        return new GetCollections(getCollectionListServiceCase, userInfoDTO, messageSource);
    }
}
