package com.k9x.configuration.secured.collections;

import com.k9x.application.collections.use_case.GetCollectionListServiceCase;
import com.k9x.application.collections.use_case.GetCollectionServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.endpoints.secured.collections.GetCollection;
import com.k9x.infrastructure.in.rest.endpoints.secured.collections.GetCollections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredCollectionsEndpointConfiguration {

    @Bean
    public GetCollection getCollection(GetCollectionServiceCase getCollectionServiceCase, UserInfoDTO userInfoDTO) {
        return new GetCollection(getCollectionServiceCase, userInfoDTO);
    }

    @Bean
    public GetCollections getCollections(GetCollectionListServiceCase getCollectionListServiceCase, UserInfoDTO userInfoDTO) {
        return new GetCollections(getCollectionListServiceCase, userInfoDTO);
    }
}
