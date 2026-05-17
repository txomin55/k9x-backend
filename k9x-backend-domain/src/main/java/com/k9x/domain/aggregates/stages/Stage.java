package com.k9x.domain.aggregates.stages;

public record Stage(
        String id,
        String name,
        String creator,
        long lastUpdate,
        long createdAt,
        Long deletedAt
) {
}
