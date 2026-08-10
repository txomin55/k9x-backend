package com.k9x.configuration.rankings;

import com.k9x.application.events.use_case.GetEventClassificationServiceCase;
import com.k9x.application.rankings.port.GetRankingPersistencePort;
import com.k9x.application.rankings.use_case.GetRankingClassificationServiceCase;
import com.k9x.infrastructure.in.rest.endpoints.rankings.FetchRankingClassification;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring for the public ranking read. Lives outside {@code configuration.secured} and takes no
 * {@code UserInfoDTO}, matching the other public endpoints.
 */
@Configuration
public class RankingsEndpointConfiguration {

    @Bean
    public GetRankingClassificationServiceCase getRankingClassificationServiceCase(
            GetRankingPersistencePort getRankingPersistencePort,
            GetEventClassificationServiceCase getEventClassificationServiceCase) {
        return new GetRankingClassificationServiceCase(getRankingPersistencePort,
                getEventClassificationServiceCase);
    }

    @Bean
    public FetchRankingClassification fetchRankingClassification(
            GetRankingClassificationServiceCase getRankingClassificationServiceCase,
            ReferenceNameResolver referenceNameResolver) {
        return new FetchRankingClassification(getRankingClassificationServiceCase, referenceNameResolver);
    }
}
