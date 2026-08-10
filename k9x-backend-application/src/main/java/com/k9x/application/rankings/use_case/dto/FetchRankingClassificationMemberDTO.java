package com.k9x.application.rankings.use_case.dto;

import java.util.List;

public record FetchRankingClassificationMemberDTO(
        String id,
        String name,
        List<FetchRankingClassificationCellDTO> cells
) {
}
