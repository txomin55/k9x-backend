package com.k9x.configuration.secured.rankings;

import com.k9x.application.rankings.port.DeleteRankingPersistencePort;
import com.k9x.application.rankings.port.GetActiveEventIdsPersistencePort;
import com.k9x.application.rankings.port.GetRankingDetailPersistencePort;
import com.k9x.application.rankings.port.GetRankingIncludeByListPort;
import com.k9x.application.rankings.port.GetRankingGroupByListPort;
import com.k9x.application.rankings.port.GetRankingPersistencePort;
import com.k9x.application.rankings.port.SaveRankingPersistencePort;
import com.k9x.application.rankings.use_case.DeleteRankingServiceCase;
import com.k9x.application.rankings.use_case.GetRankingIncludeByListServiceCase;
import com.k9x.application.rankings.use_case.GetRankingGroupByListServiceCase;
import com.k9x.application.rankings.use_case.GetRankingServiceCase;
import com.k9x.application.rankings.use_case.SaveRankingServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RankingUseCaseConfiguration {

    @Bean
    public SaveRankingServiceCase saveRankingServiceCase(
            GetRankingPersistencePort getRankingPersistencePort,
            GetActiveEventIdsPersistencePort getActiveEventIdsPersistencePort,
            SaveRankingPersistencePort saveRankingPersistencePort,
            DeleteRankingPersistencePort deleteRankingPersistencePort) {
        return new SaveRankingServiceCase(getRankingPersistencePort, getActiveEventIdsPersistencePort,
                saveRankingPersistencePort, deleteRankingPersistencePort);
    }

    @Bean
    public GetRankingServiceCase getRankingServiceCase(
            GetRankingDetailPersistencePort getRankingDetailPersistencePort) {
        return new GetRankingServiceCase(getRankingDetailPersistencePort);
    }

    @Bean
    public DeleteRankingServiceCase deleteRankingServiceCase(
            GetRankingPersistencePort getRankingPersistencePort,
            DeleteRankingPersistencePort deleteRankingPersistencePort) {
        return new DeleteRankingServiceCase(getRankingPersistencePort, deleteRankingPersistencePort);
    }

    @Bean
    public GetRankingGroupByListServiceCase getRankingGroupByListServiceCase(
            GetRankingGroupByListPort getRankingGroupByListPort) {
        return new GetRankingGroupByListServiceCase(getRankingGroupByListPort);
    }

    @Bean
    public GetRankingIncludeByListServiceCase getRankingIncludeByListServiceCase(
            GetRankingIncludeByListPort getRankingIncludeByListPort) {
        return new GetRankingIncludeByListServiceCase(getRankingIncludeByListPort);
    }
}
