package com.k9x.configuration.secured.rankings;

import com.k9x.application.rankings.use_case.DeleteRankingServiceCase;
import com.k9x.application.rankings.use_case.GetRankingIncludeByListServiceCase;
import com.k9x.application.rankings.use_case.GetRankingGroupByListServiceCase;
import com.k9x.application.rankings.use_case.GetRankingListServiceCase;
import com.k9x.application.rankings.use_case.GetRankingServiceCase;
import com.k9x.application.rankings.use_case.SaveRankingServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.endpoints.secured.rankings.CreateRanking;
import com.k9x.infrastructure.in.rest.endpoints.secured.rankings.FetchRanking;
import com.k9x.infrastructure.in.rest.endpoints.secured.rankings.FetchRankings;
import com.k9x.infrastructure.in.rest.endpoints.secured.rankings.FetchRankingIncludeBys;
import com.k9x.infrastructure.in.rest.endpoints.secured.rankings.FetchRankingGroupBys;
import com.k9x.infrastructure.in.rest.endpoints.secured.rankings.RemoveRanking;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredRankingsEndpointConfiguration {

    @Bean
    public CreateRanking createRanking(SaveRankingServiceCase saveRankingServiceCase, UserInfoDTO userInfoDTO) {
        return new CreateRanking(saveRankingServiceCase, userInfoDTO);
    }

    @Bean
    public FetchRanking fetchRanking(GetRankingServiceCase getRankingServiceCase, UserInfoDTO userInfoDTO) {
        return new FetchRanking(getRankingServiceCase, userInfoDTO);
    }

    @Bean
    public FetchRankings fetchRankings(GetRankingListServiceCase getRankingListServiceCase,
                                       UserInfoDTO userInfoDTO) {
        return new FetchRankings(getRankingListServiceCase, userInfoDTO);
    }

    @Bean
    public RemoveRanking removeRanking(DeleteRankingServiceCase deleteRankingServiceCase, UserInfoDTO userInfoDTO) {
        return new RemoveRanking(deleteRankingServiceCase, userInfoDTO);
    }

    // The criteria catalogues carry no user: like breeds and countries they are reference data, not a CRUD
    // operation on a ranking.
    @Bean
    public FetchRankingGroupBys fetchRankingGroupBys(
            GetRankingGroupByListServiceCase getRankingGroupByListServiceCase) {
        return new FetchRankingGroupBys(getRankingGroupByListServiceCase);
    }

    @Bean
    public FetchRankingIncludeBys fetchRankingIncludeBys(
            GetRankingIncludeByListServiceCase getRankingIncludeByListServiceCase) {
        return new FetchRankingIncludeBys(getRankingIncludeByListServiceCase);
    }
}
