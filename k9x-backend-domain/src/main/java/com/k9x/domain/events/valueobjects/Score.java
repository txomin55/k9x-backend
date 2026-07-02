package com.k9x.domain.events.valueobjects;

import java.math.BigDecimal;

public record Score(
        String exerciseId,
        String judgeId,
        String dogId,
        BigDecimal score,
        long lastUpdate,
        Long yellowCard
) {
    public Score(String exerciseId, String judgeId, String dogId, BigDecimal score, long lastUpdate) {
        this(exerciseId, judgeId, dogId, score, lastUpdate, null);
    }
}
