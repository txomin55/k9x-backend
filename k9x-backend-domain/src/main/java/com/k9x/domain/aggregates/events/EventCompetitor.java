package com.k9x.domain.aggregates.events;

public record EventCompetitor(
        String dogId,
        String dogName,
        String owner,
        String team,
        String country,
        String breed,
        String identity,
        Short position,
        Boolean verified,
        boolean notCompeting
) {
}
