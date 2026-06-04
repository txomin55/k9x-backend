package com.k9x.application.collections.obdx.use_case.dto;

import java.math.BigDecimal;

public record FetchCollectionJudgeScoreDTO(BigDecimal score, String judgeId, String judgeName) {}
