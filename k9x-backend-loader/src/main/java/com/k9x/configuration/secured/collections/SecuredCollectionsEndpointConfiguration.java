package com.k9x.configuration.secured.collections;

import com.k9x.application.collections.obdx.use_case.GetObdxRedCardServiceCase;
import com.k9x.application.collections.obdx.use_case.GetObdxYellowCardsServiceCase;
import com.k9x.application.collections.obdx.use_case.RegisterObdxRedCardServiceCase;
import com.k9x.application.collections.obdx.use_case.RegisterObdxYellowCardServiceCase;
import com.k9x.application.collections.use_case.GetCollectionListServiceCase;
import com.k9x.application.collections.use_case.GetCollectionServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.endpoints.secured.collections.GetCollection;
import com.k9x.infrastructure.in.rest.endpoints.secured.collections.GetCollections;
import com.k9x.infrastructure.in.rest.endpoints.secured.collections.obdx.GetObdxRedCard;
import com.k9x.infrastructure.in.rest.endpoints.secured.collections.obdx.GetObdxYellowCards;
import com.k9x.infrastructure.in.rest.endpoints.secured.collections.obdx.RegisterObdxRedCard;
import com.k9x.infrastructure.in.rest.endpoints.secured.collections.obdx.RegisterObdxYellowCard;
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

    @Bean
    public RegisterObdxYellowCard registerYellowCard(RegisterObdxYellowCardServiceCase registerObdxYellowCardServiceCase, UserInfoDTO userInfoDTO) {
        return new RegisterObdxYellowCard(registerObdxYellowCardServiceCase, userInfoDTO);
    }

    @Bean
    public GetObdxYellowCards getYellowCards(GetObdxYellowCardsServiceCase getObdxYellowCardsServiceCase, MessageSource messageSource) {
        return new GetObdxYellowCards(getObdxYellowCardsServiceCase, messageSource);
    }

    @Bean
    public RegisterObdxRedCard registerRedCard(RegisterObdxRedCardServiceCase registerObdxRedCardServiceCase, UserInfoDTO userInfoDTO) {
        return new RegisterObdxRedCard(registerObdxRedCardServiceCase, userInfoDTO);
    }

    @Bean
    public GetObdxRedCard getRedCard(GetObdxRedCardServiceCase getObdxRedCardServiceCase, MessageSource messageSource) {
        return new GetObdxRedCard(getObdxRedCardServiceCase, messageSource);
    }
}
