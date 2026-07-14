package com.k9x.infrastructure.in.scheduler;

import com.k9x.application.events.snapshot.use_case.GenerateEventSnapshotsServiceCase;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Inbound adapter that triggers the daily generation of classification snapshots for finished events.
 * Runs at 01:00 UTC by default (after the UTC day rollover, matching the UTC day-boundary reasoning);
 * override with the {@code k9x.snapshot.cron} property.
 */
public class EventSnapshotScheduler {

    private final GenerateEventSnapshotsServiceCase generateEventSnapshotsServiceCase;

    public EventSnapshotScheduler(GenerateEventSnapshotsServiceCase generateEventSnapshotsServiceCase) {
        this.generateEventSnapshotsServiceCase = generateEventSnapshotsServiceCase;
    }

    @Scheduled(cron = "${k9x.snapshot.cron:0 0 1 * * *}", zone = "UTC")
    public void generateSnapshots() {
        generateEventSnapshotsServiceCase.generateSnapshots();
    }
}
