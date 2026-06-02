package com.k9x.domain.aggregates.events;

import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;

public record Event(
        String id,
        String configurationId,
        String discipline,
        String name,
        String stageId,
        String creator,
        long lastUpdate,
        long createdAt,
        Long deletedAt,
        ObdxAvgMethod scoreCalculation
) {
}
