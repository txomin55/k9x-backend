package com.k9x.domain.events.valueobjects;

import java.math.BigDecimal;

public record Score(
        String exerciseId,
        String judgeId,
        String dogId,
        BigDecimal score,
        long lastUpdate
) {
}
