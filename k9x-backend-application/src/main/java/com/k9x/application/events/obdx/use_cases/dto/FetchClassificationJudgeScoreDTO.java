package com.k9x.application.events.obdx.use_cases.dto;

import java.math.BigDecimal;

public record FetchClassificationJudgeScoreDTO(String judgeId, String judgeName, BigDecimal score,
                                               BigDecimal scoreRating) {
}
