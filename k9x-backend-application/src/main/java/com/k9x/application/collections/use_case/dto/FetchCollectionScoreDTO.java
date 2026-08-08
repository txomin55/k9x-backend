package com.k9x.application.collections.use_case.dto;

import java.math.BigDecimal;

public record FetchCollectionScoreDTO(String dogIdentification, String exerciseId, String judgeId, BigDecimal score,
                                      Long yellowCard, Long redCard) {}
