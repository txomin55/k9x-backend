package com.k9x.domain.events.valueobjects;

public record EventCompetitor(
        String dogId,
        String dogName,
        String owner,
        String handler,
        String team,
        String country,
        String breed,
        String identity,
        Short startNumber,
        Short competitorNumber,
        Boolean verified,
        boolean notCompeting,
        Boolean bih,
        Boolean reserve,
        Boolean threeFciGenerationsConfirmed
) {
}
