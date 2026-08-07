package com.k9x.domain.events.valueobjects;

import com.k9x.domain.dogs.aggregates.Sex;

public record EventCompetitor(
        String dogId,
        String dogName,
        String owner,
        String handler,
        String team,
        String country,
        String breed,
        String identity,
        Sex sex,
        Short startNumber,
        Short competitorNumber,
        Boolean verified,
        boolean notCompeting,
        Boolean bih,
        Boolean reserve,
        Boolean threeFciGenerationsConfirmed
) {
}
