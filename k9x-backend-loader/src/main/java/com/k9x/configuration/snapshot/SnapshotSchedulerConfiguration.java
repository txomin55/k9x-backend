package com.k9x.configuration.snapshot;

import com.k9x.application.events.snapshot.use_case.GenerateEventSnapshotsServiceCase;
import com.k9x.infrastructure.in.scheduler.EventSnapshotScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SnapshotSchedulerConfiguration {

    @Bean
    public EventSnapshotScheduler eventSnapshotScheduler(
            GenerateEventSnapshotsServiceCase generateEventSnapshotsServiceCase) {
        return new EventSnapshotScheduler(generateEventSnapshotsServiceCase);
    }
}
