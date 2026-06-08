package com.k9x.domain.aggregates.events;

import java.math.BigDecimal;

public record Score(
        String exerciseId,
        String judgeId,
        String dogId,
        BigDecimal score,
        long lastUpdate
) {
}
