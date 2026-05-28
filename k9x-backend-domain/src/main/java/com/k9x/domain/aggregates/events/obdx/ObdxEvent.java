package com.k9x.domain.aggregates.events.obdx;

public record ObdxEvent(
        String id,
        String configurationId,
        String name,
        String stageId,
        String creator,
        long lastUpdate,
        long createdAt,
        Long deletedAt
) {
}
