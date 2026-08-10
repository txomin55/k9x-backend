package com.k9x.application.rankings.use_case.dto;

import java.math.BigDecimal;

public record FetchRankingClassificationCellDTO(String eventId, BigDecimal score, boolean counts) {
}
