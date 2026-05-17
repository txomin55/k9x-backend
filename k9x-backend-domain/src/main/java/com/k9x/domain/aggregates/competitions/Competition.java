package com.k9x.domain.aggregates.competitions;

public record Competition(
        String id,
        String name,
        String creator,
        long lastUpdate,
        long createdAt,
        Long deletedAt
) {
}
