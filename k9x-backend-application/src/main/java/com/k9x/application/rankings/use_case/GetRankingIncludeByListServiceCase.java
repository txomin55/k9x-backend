package com.k9x.application.rankings.use_case;

import com.k9x.application.rankings.port.GetRankingIncludeByListPort;
import com.k9x.application.rankings.use_case.dto.RankingCriterionDTO;

import java.util.List;

public class GetRankingIncludeByListServiceCase {

    private final GetRankingIncludeByListPort getRankingIncludeByListPort;

    public GetRankingIncludeByListServiceCase(GetRankingIncludeByListPort getRankingIncludeByListPort) {
        this.getRankingIncludeByListPort = getRankingIncludeByListPort;
    }

    public List<RankingCriterionDTO> getIncludeBys() {
        return getRankingIncludeByListPort.getIncludeBys();
    }
}
