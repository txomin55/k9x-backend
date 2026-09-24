package com.k9x.application.events.use_case;

import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.dto.ConfigurationDTO;
import com.k9x.application.events.obdx.use_case.GetObdxClassificationServiceCase;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.port.GetClassificationEventPersistencePort;
import com.k9x.application.events.port.GetEventClassificationHeaderPersistencePort;
import com.k9x.application.events.snapshot.use_case.GetEventSnapshotServiceCase;
import com.k9x.application.events.use_case.dto.EventClassificationContextDTO;
import com.k9x.application.events.use_case.dto.FetchEventClassificationHeaderDTO;
import com.k9x.application.events.use_case.port.EventClassificationCacheManagerPort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.disciplines.exceptions.DisciplineConfigurationMalformedException;
import com.k9x.domain.disciplines.obdx.ObdxRank;
import com.k9x.domain.disciplines.valueobjects.Discipline;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.exceptions.EventAlreadyDeletedException;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.domain.events.status.EventLifecycle;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An event's classification. It reads a header projection (event, stage, competition) instead of the competition
 * aggregate, and loads the event's scores only when they are needed: to compute a live classification, or to ask
 * the domain whether every competitor is settled while the stage has not finished by date. A finished event with
 * a stored snapshot never loads a score.
 */
public class GetEventClassificationServiceCase {

    private static final int EVENT_CONTEXT_TTL_SECONDS = 30;

    private final GetEventClassificationHeaderPersistencePort getEventClassificationHeaderPersistencePort;
    private final GetClassificationEventPersistencePort getClassificationEventPersistencePort;
    private final EventClassificationCacheManagerPort eventClassificationCacheManagerPort;
    private final GetObdxClassificationServiceCase getObdxClassificationServiceCase;
    private final GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort;
    private final GetEventSnapshotServiceCase getEventSnapshotServiceCase;

    public GetEventClassificationServiceCase(
            GetEventClassificationHeaderPersistencePort getEventClassificationHeaderPersistencePort,
            GetClassificationEventPersistencePort getClassificationEventPersistencePort,
            EventClassificationCacheManagerPort eventClassificationCacheManagerPort,
            GetObdxClassificationServiceCase getObdxClassificationServiceCase,
            GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort,
            GetEventSnapshotServiceCase getEventSnapshotServiceCase) {
        this.getEventClassificationHeaderPersistencePort = getEventClassificationHeaderPersistencePort;
        this.getClassificationEventPersistencePort = getClassificationEventPersistencePort;
        this.eventClassificationCacheManagerPort = eventClassificationCacheManagerPort;
        this.getObdxClassificationServiceCase = getObdxClassificationServiceCase;
        this.getObdxFederationsConfigurationsPort = getObdxFederationsConfigurationsPort;
        this.getEventSnapshotServiceCase = getEventSnapshotServiceCase;
    }

    public FetchClassificationDTO getClassification(String eventId) {
        LoadedContext context = new LoadedContext(eventId, resolveContext(eventId));
        FetchEventClassificationHeaderDTO header = context.header();

        // The snapshot caches only the heavy OBDX computation (competitor totals, positions, per-exercise
        // scores). A persisted snapshot exists only for finished events; their results are final, so serve the
        // stored payload and skip recomputation. Everything else — event metadata and the derived rank label —
        // is always rebuilt fresh here from the header, never stored.
        Optional<FetchObdxClassificationDTO> stored = getEventSnapshotServiceCase.getSnapshot(eventId, header.disciplineId());
        FetchObdxClassificationDTO obdx = stored.orElseGet(() ->
                Discipline.fromStored(header.disciplineId()) == Discipline.OBDX
                        ? getObdxClassificationServiceCase.getClassification(context.event())
                        : null);

        Long scoresLastUpdate = obdx == null ? null : obdx.scoresLastUpdate();

        Map<String, String> configNameById = buildConfigNameMap();
        String configurationName = configNameById.getOrDefault(header.configurationId(), header.configurationId());

        long now = DateUtils.nowUtcMillis();
        String status = EventLifecycle.status(header.deletedAt(), now, header.stageDateTo(),
                () -> context.event().allCompetitorsSettled(), header::hasAnyScore).name();
        return new FetchClassificationDTO(eventId, header.name(), status,
                header.stageId(), header.stageName(), header.competitionName(), header.disciplineId(),
                header.configurationId(), configurationName, scoresLastUpdate, obdx,
                header.rankScore() == null ? null : ObdxRank.labelFromScore(header.rankScore()),
                header.competitionExtraction());
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

        FetchEventClassificationHeaderDTO header = getEventClassificationHeaderPersistencePort.getHeader(eventId)
                .orElseThrow(EventNotFoundException::new);
        if (header.deletedAt() != null) throw new EventAlreadyDeletedException();

        EventClassificationContextDTO context = new EventClassificationContextDTO(header, null);
        eventClassificationCacheManagerPort.put(eventId, context);
        return context;
    }

    /** The context of one call, loading the event's scores at most once and only if something asks for them. */
    private final class LoadedContext {

        private final String eventId;
        private EventClassificationContextDTO context;

        private LoadedContext(String eventId, EventClassificationContextDTO context) {
            this.eventId = eventId;
            this.context = context;
        }

        FetchEventClassificationHeaderDTO header() {
            return context.header();
        }

        EventSnapshot event() {
            if (context.event() == null) {
                EventSnapshot loaded = getClassificationEventPersistencePort.getEvent(eventId)
                        .orElseThrow(EventNotFoundException::new);
                context = context.withEvent(loaded);
                eventClassificationCacheManagerPort.put(eventId, context);
            }
            return context.event();
        }
    }
}
