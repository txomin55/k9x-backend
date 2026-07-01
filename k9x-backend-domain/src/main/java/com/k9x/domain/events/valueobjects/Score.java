package com.k9x.domain.events.valueobjects;

import java.math.BigDecimal;

public record Score(
        String exerciseId,
        String judgeId,
        String dogId,
        BigDecimal score,
        long lastUpdate,
        Long yellowCard1,
        Long yellowCard2
) {
    public Score(String exerciseId, String judgeId, String dogId, BigDecimal score, long lastUpdate) {
        this(exerciseId, judgeId, dogId, score, lastUpdate, null, null);
    }
}
