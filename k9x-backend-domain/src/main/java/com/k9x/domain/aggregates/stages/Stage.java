package com.k9x.domain.aggregates.stages;

public record Stage(
        String id,
        String name,
        String competitionId,
        String creator,
        long dateTo,
        long lastUpdate,
        long createdAt,
        Long deletedAt
) {
}
