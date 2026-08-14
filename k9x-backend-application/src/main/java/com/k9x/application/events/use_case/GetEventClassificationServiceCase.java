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
import com.k9x.application.events.snapshot.use_case.GetEventSnapshotServiceCase;
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
    private final GetEventSnapshotServiceCase getEventSnapshotServiceCase;

    public GetEventClassificationServiceCase(
            GetCompetitionPersistencePort getCompetitionPersistencePort,
            EventClassificationCacheManagerPort eventClassificationCacheManagerPort,
            GetObdxClassificationServiceCase getObdxClassificationServiceCase,
            GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort,
            GetEventSnapshotServiceCase getEventSnapshotServiceCase) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.eventClassificationCacheManagerPort = eventClassificationCacheManagerPort;
        this.getObdxClassificationServiceCase = getObdxClassificationServiceCase;
        this.getObdxFederationsConfigurationsPort = getObdxFederationsConfigurationsPort;
        this.getEventSnapshotServiceCase = getEventSnapshotServiceCase;
    }

    public FetchClassificationDTO getClassification(String eventId) {
        EventClassificationContextDTO context = resolveContext(eventId);
        EventSnapshot event = context.event();

        // The snapshot caches only the heavy OBDX computation (competitor totals, positions, per-exercise
        // scores). A persisted snapshot exists only for finished events; their results are final, so serve the
        // stored payload and skip recomputation. Everything else — event metadata and the derived rank label —
        // is always rebuilt fresh here from the event context (the joins in resolveContext), never stored.
        Optional<FetchObdxClassificationDTO> stored = getEventSnapshotServiceCase.getSnapshot(eventId, event.discipline());
        FetchObdxClassificationDTO obdx = stored.orElseGet(() ->
                Discipline.fromStored(event.discipline()) == Discipline.OBDX
                        ? getObdxClassificationServiceCase.getClassification(event)
                        : null);

        Long scoresLastUpdate = obdx == null ? null : obdx.scoresLastUpdate();

        Map<String, String> configNameById = buildConfigNameMap();
        String configurationName = configNameById.getOrDefault(event.configurationId(), event.configurationId());

        long now = DateUtils.nowUtcMillis();
        return new FetchClassificationDTO(eventId, event.name(), event.status(now, context.stageDateTo()).name(),
                event.stageId(), context.stageName(), context.competitionName(), event.discipline(),
                event.configurationId(), configurationName, scoresLastUpdate, obdx, event.rank(),
                context.competitionSource());
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
                new EventClassificationContextDTO(event, stage.name(), stage.dateTo(), competition.name(),
                        competition.source());
        eventClassificationCacheManagerPort.put(eventId, context);
        return context;
    }
}
