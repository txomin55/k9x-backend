package com.k9x.application.events.use_case;

import com.k9x.application.competitions.CompetitionNavigator;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.dto.ConfigurationDTO;
import com.k9x.domain.disciplines.exceptions.DisciplineConfigurationMalformedException;
import com.k9x.domain.events.exceptions.EventAlreadyDeletedException;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.use_case.GetObdxClassificationServiceCase;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.snapshot.port.GetEventSnapshotPersistencePort;
import com.k9x.application.events.use_case.dto.EventClassificationContextDTO;
import com.k9x.application.events.use_case.port.EventClassificationCacheManagerPort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.disciplines.valueobjects.Discipline;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.stages.aggregates.StageSnapshot;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GetEventClassificationServiceCase {

    private static final int EVENT_CONTEXT_TTL_SECONDS = 30;

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final EventClassificationCacheManagerPort eventClassificationCacheManagerPort;
    private final GetObdxClassificationServiceCase getObdxClassificationServiceCase;
    private final GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort;
    private final GetEventSnapshotPersistencePort getEventSnapshotPersistencePort;

    public GetEventClassificationServiceCase(
            GetCompetitionPersistencePort getCompetitionPersistencePort,
            EventClassificationCacheManagerPort eventClassificationCacheManagerPort,
            GetObdxClassificationServiceCase getObdxClassificationServiceCase,
            GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort,
            GetEventSnapshotPersistencePort getEventSnapshotPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.eventClassificationCacheManagerPort = eventClassificationCacheManagerPort;
        this.getObdxClassificationServiceCase = getObdxClassificationServiceCase;
        this.getObdxFederationsConfigurationsPort = getObdxFederationsConfigurationsPort;
        this.getEventSnapshotPersistencePort = getEventSnapshotPersistencePort;
    }

    public FetchClassificationDTO getClassification(String eventId) {
        // A persisted snapshot exists only for events whose stage has already finished; their results are final,
        // so serve the stored classification verbatim and skip aggregate hydration and recomputation. The REST
        // layer still applies i18n on top, so the snapshot stays language-independent.
        Optional<FetchClassificationDTO> snapshot = getEventSnapshotPersistencePort.getSnapshot(eventId);
        if (snapshot.isPresent()) {
            return snapshot.get();
        }

        EventClassificationContextDTO context = resolveContext(eventId);
        EventSnapshot event = context.event();

        Discipline discipline = Discipline.fromStored(event.discipline());
        FetchObdxClassificationDTO obdx = discipline == Discipline.OBDX
                ? getObdxClassificationServiceCase.getClassification(event)
                : null;

        Long scoresLastUpdate = obdx == null ? null : obdx.scoresLastUpdate();

        Map<String, String> configNameById = buildConfigNameMap();
        String configurationName = configNameById.getOrDefault(event.configurationId(), event.configurationId());

        long now = DateUtils.nowUtcMillis();
        return new FetchClassificationDTO(eventId, event.name(), event.status(now, context.stageDateTo()).name(),
                event.stageId(), context.stageName(), context.competitionName(), event.discipline(),
                event.configurationId(), configurationName, scoresLastUpdate, obdx);
    }

    private Map<String, String> buildConfigNameMap() {
        try {
            return getObdxFederationsConfigurationsPort.getConfigurations().stream()
                    .flatMap(f -> f.configurations().stream())
                    .collect(Collectors.toMap(ConfigurationDTO::id, ConfigurationDTO::name, (a, _) -> a));
        } catch (IOException e) {
            throw new DisciplineConfigurationMalformedException();
        }
    }

    private EventClassificationContextDTO resolveContext(String eventId) {
        EventClassificationContextDTO cached =
                eventClassificationCacheManagerPort.getIfPresentAndValid(eventId, EVENT_CONTEXT_TTL_SECONDS);
        if (cached != null) {
            return cached;
        }

        String competitionId = getCompetitionPersistencePort.competitionIdByEvent(eventId);
        if (competitionId == null) throw new EventNotFoundException();
        CompetitionSnapshot competition = getCompetitionPersistencePort.getCompetition(competitionId);
        EventSnapshot event = CompetitionNavigator.findEvent(competition, eventId);
        if (event == null) throw new EventNotFoundException();
        if (event.deletedAt() != null) throw new EventAlreadyDeletedException();

        StageSnapshot stage = CompetitionNavigator.findStageOfEvent(competition, eventId);

        EventClassificationContextDTO context =
                new EventClassificationContextDTO(event, stage.name(), stage.dateTo(), competition.name());
        eventClassificationCacheManagerPort.put(eventId, context);
        return context;
    }
}
