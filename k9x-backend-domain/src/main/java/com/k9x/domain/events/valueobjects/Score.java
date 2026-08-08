package com.k9x.domain.events.valueobjects;

import java.math.BigDecimal;

public record Score(
        String exerciseId,
        String judgeId,
        String dogIdentification,
        BigDecimal score,
        long lastUpdate,
        Long yellowCard,
        Long redCard
) {
    public Score(String exerciseId, String judgeId, String dogIdentification, BigDecimal score, long lastUpdate) {
        this(exerciseId, judgeId, dogIdentification, score, lastUpdate, null, null);
    }

    public Score(String exerciseId, String judgeId, String dogIdentification, BigDecimal score, long lastUpdate, Long yellowCard) {
        this(exerciseId, judgeId, dogIdentification, score, lastUpdate, yellowCard, null);
    }
}
