package com.k9x.domain.aggregates.events.obdx;

import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;

public record ObdxEvent(
        String id,
        String configurationId,
        String name,
        String stageId,
        String creator,
        long lastUpdate,
        long createdAt,
        Long deletedAt,
        ObdxAvgMethod scoreCalculation
) {
}
