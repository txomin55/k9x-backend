package com.k9x.domain.events.valueobjects;

import java.math.BigDecimal;

public record EventCompetitor(
        String dogId,
        String dogName,
        String owner,
        String handler,
        String team,
        String country,
        String breed,
        String identity,
        Short position,
        Boolean verified,
        boolean notCompeting,
        BigDecimal finalScore,
        Boolean bih,
        Boolean threeFciGenerationsConfirmed
) {
}
