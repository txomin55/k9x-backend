package com.k9x.domain.competitions.aggregates;

import com.k9x.domain.competitions.status.CompetitionStatus;

import com.k9x.domain.stages.aggregates.StageSnapshot;
import com.k9x.domain.stages.status.StageStatus;

import java.util.List;

public record CompetitionSnapshot(
        String id,
        String name,
        String creator,
        String organizerName,
        String country,
        String description,
        String address,
        Double coordAlt,
        Double coordLong,
        CompetitionSource source,
        CompetitionExtraction extractionMetadata,
        long lastUpdate,
        long createdAt,
        Long deletedAt,
        List<StageSnapshot> stages
) {

    public CompetitionSnapshot {
        source = source == null ? CompetitionSource.API : source;
    }

    /**
     * Provenance to surface to the reader, {@code null} for competitions created through the app. An EXTRACTION
     * competition with no metadata row still yields {@link CompetitionExtraction#UNKNOWN}: the warning does not
     * depend on anybody having written down where the data came from.
     */
    public CompetitionExtraction extraction() {
        if (source != CompetitionSource.EXTRACTION) {
            return null;
        }
        return extractionMetadata == null ? CompetitionExtraction.UNKNOWN : extractionMetadata;
    }

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
