package com.k9x.domain.aggregates.competitions;

import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.domain.aggregates.stages.StageStatus;

import java.util.List;

public record Competition(
        String id,
        String name,
        String creator,
        String organizerName,
        String country,
        String description,
        String address,
        Double coordAlt,
        Double coordLong,
        long lastUpdate,
        long createdAt,
        Long deletedAt,
        List<Stage> stages
) {

    /**
     * Lifecycle status derived from its (non-deleted) stages: FINISHED when they are all finished,
     * STARTED when any is to-start or started, otherwise CREATED.
     */
    public CompetitionStatus status(long now) {
        if (deletedAt != null) {
            return CompetitionStatus.DELETED;
        }
        List<StageStatus> activeStageStatuses = stages == null ? List.of() : stages.stream()
                .filter(s -> s.deletedAt() == null)
                .map(s -> s.status(now))
                .toList();
        if (activeStageStatuses.isEmpty()) {
            return CompetitionStatus.CREATED;
        }
        if (activeStageStatuses.stream().allMatch(s -> s == StageStatus.FINISHED)) {
            return CompetitionStatus.FINISHED;
        }
        if (activeStageStatuses.stream().anyMatch(s -> s == StageStatus.TO_START || s == StageStatus.STARTED)) {
            return CompetitionStatus.STARTED;
        }
        return CompetitionStatus.CREATED;
    }
}
