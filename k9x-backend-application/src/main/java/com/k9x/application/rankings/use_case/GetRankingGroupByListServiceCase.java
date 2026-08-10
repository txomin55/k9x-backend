package com.k9x.application.rankings.use_case;

import com.k9x.application.rankings.port.GetRankingGroupByListPort;
import com.k9x.application.rankings.use_case.dto.RankingCriterionDTO;

import java.util.List;

/**
 * Reference catalogue of grouping criteria, following the same shape as breeds and countries: no
 * organizer gate and no user, because it is not a CRUD operation on a ranking.
 */
public class GetRankingGroupByListServiceCase {

    private final GetRankingGroupByListPort getRankingGroupByListPort;

    public GetRankingGroupByListServiceCase(GetRankingGroupByListPort getRankingGroupByListPort) {
        this.getRankingGroupByListPort = getRankingGroupByListPort;
    }

    public List<RankingCriterionDTO> getGroupBys() {
        return getRankingGroupByListPort.getGroupBys();
    }
}
