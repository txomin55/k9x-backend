package com.k9x.application.rankings.use_case.dto;

import java.math.BigDecimal;
import java.util.List;

public record FetchRankingClassificationGroupDTO(
        String id,
        String name,
        int position,
        boolean tied,
        BigDecimal total,
        List<FetchRankingClassificationMemberDTO> members
) {
}
