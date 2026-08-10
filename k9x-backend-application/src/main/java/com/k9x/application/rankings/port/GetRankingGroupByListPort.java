package com.k9x.application.rankings.port;

import com.k9x.application.rankings.use_case.dto.RankingCriterionDTO;

import java.util.List;

public interface GetRankingGroupByListPort {

    List<RankingCriterionDTO> getGroupBys();
}
