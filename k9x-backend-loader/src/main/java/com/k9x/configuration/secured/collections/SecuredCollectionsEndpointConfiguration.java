package com.k9x.configuration.secured.collections;

import com.k9x.application.collections.use_case.GetCollectionListServiceCase;
import com.k9x.application.collections.use_case.GetObdxCollectionServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.endpoints.secured.collections.GetCollections;
import com.k9x.infrastructure.in.rest.endpoints.secured.collections.obdx.GetObdxCollection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredCollectionsEndpointConfiguration {

    @Bean
    public GetObdxCollection getObdxCollection(GetObdxCollectionServiceCase getObdxCollectionServiceCase, UserInfoDTO userInfoDTO) {
        return new GetObdxCollection(getObdxCollectionServiceCase, userInfoDTO);
    }

    @Bean
    public GetCollections getCollections(GetCollectionListServiceCase getCollectionListServiceCase, UserInfoDTO userInfoDTO) {
        return new GetCollections(getCollectionListServiceCase, userInfoDTO);
    }
}
