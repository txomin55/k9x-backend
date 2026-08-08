package com.k9x.domain.events.valueobjects;

import com.k9x.domain.dogs.aggregates.Sex;

public record EventCompetitor(
        String dogIdentification,
        String dogName,
        String owner,
        String handler,
        String team,
        String country,
        String breed,
        String origin,
        String license,
        Sex sex,
        Short startNumber,
        Short competitorNumber,
        Boolean verified,
        boolean notCompeting,
        Boolean bih,
        String primer,
        Boolean reserve,
        Boolean threeFciGenerationsConfirmed
) {
}
