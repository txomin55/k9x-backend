package com.k9x.configuration.snapshot;

import com.k9x.application.events.snapshot.port.GetObdxEventSnapshotPersistencePort;
import com.k9x.application.events.snapshot.port.GetPendingSnapshotEventsPersistencePort;
import com.k9x.application.events.snapshot.port.SaveObdxEventSnapshotPersistencePort;
import com.k9x.application.events.snapshot.port.UpdateObdxCompetitorPositionsPersistencePort;
import com.k9x.application.events.snapshot.use_case.GenerateEventSnapshotsServiceCase;
import com.k9x.application.events.snapshot.use_case.GetEventSnapshotServiceCase;
import com.k9x.application.events.use_case.GetEventClassificationServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnapshotUseCaseConfiguration {

    @Bean
    public GetEventSnapshotServiceCase getEventSnapshotServiceCase(
            GetObdxEventSnapshotPersistencePort getObdxEventSnapshotPersistencePort) {
        return new GetEventSnapshotServiceCase(getObdxEventSnapshotPersistencePort);
    }

    @Bean
    public GenerateEventSnapshotsServiceCase generateEventSnapshotsServiceCase(
            GetPendingSnapshotEventsPersistencePort getPendingSnapshotEventsPersistencePort,
            GetEventClassificationServiceCase getEventClassificationServiceCase,
            SaveObdxEventSnapshotPersistencePort saveObdxEventSnapshotPersistencePort,
            UpdateObdxCompetitorPositionsPersistencePort updateObdxCompetitorPositionsPersistencePort) {
        return new GenerateEventSnapshotsServiceCase(
                getPendingSnapshotEventsPersistencePort,
                getEventClassificationServiceCase,
                saveObdxEventSnapshotPersistencePort,
                updateObdxCompetitorPositionsPersistencePort);
    }
}
