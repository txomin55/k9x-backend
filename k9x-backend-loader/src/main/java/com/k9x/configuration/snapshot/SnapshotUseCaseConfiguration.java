package com.k9x.configuration.snapshot;

import com.k9x.application.events.snapshot.port.GetPendingSnapshotEventsPersistencePort;
import com.k9x.application.events.snapshot.port.SaveEventSnapshotPersistencePort;
import com.k9x.application.events.snapshot.use_case.GenerateEventSnapshotsServiceCase;
import com.k9x.application.events.use_case.GetEventClassificationServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnapshotUseCaseConfiguration {

    @Bean
    public GenerateEventSnapshotsServiceCase generateEventSnapshotsServiceCase(
            GetPendingSnapshotEventsPersistencePort getPendingSnapshotEventsPersistencePort,
            GetEventClassificationServiceCase getEventClassificationServiceCase,
            SaveEventSnapshotPersistencePort saveEventSnapshotPersistencePort) {
        return new GenerateEventSnapshotsServiceCase(
                getPendingSnapshotEventsPersistencePort,
                getEventClassificationServiceCase,
                saveEventSnapshotPersistencePort);
    }
}
