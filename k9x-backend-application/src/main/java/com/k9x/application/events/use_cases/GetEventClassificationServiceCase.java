package com.k9x.application.events.use_cases;

import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.use_cases.GetObdxClassificationServiceCase;
import com.k9x.application.events.obdx.use_cases.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_cases.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.obdx.use_cases.port.GetEventPersistencePort;
import com.k9x.application.events.use_cases.dto.EventClassificationContextDTO;
import com.k9x.application.events.use_cases.port.EventClassificationCacheManagerPort;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.domain.aggregates.disciplines.Discipline;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.stages.Stage;

import java.util.Locale;

public class GetEventClassificationServiceCase {

    private static final int EVENT_CONTEXT_TTL_SECONDS = 30;

    private final GetEventPersistencePort getEventPersistencePort;
    private final GetStagePersistencePort getStagePersistencePort;
    private final EventClassificationCacheManagerPort eventClassificationCacheManagerPort;
    private final GetObdxClassificationServiceCase getObdxClassificationServiceCase;

    public GetEventClassificationServiceCase(
            GetEventPersistencePort getEventPersistencePort,
            GetStagePersistencePort getStagePersistencePort,
            EventClassificationCacheManagerPort eventClassificationCacheManagerPort,
            GetObdxClassificationServiceCase getObdxClassificationServiceCase) {
        this.getEventPersistencePort = getEventPersistencePort;
        this.getStagePersistencePort = getStagePersistencePort;
        this.eventClassificationCacheManagerPort = eventClassificationCacheManagerPort;
        this.getObdxClassificationServiceCase = getObdxClassificationServiceCase;
    }

    public FetchClassificationDTO getClassification(String eventId) {
        EventClassificationContextDTO context = resolveContext(eventId);
        Event event = context.event();

        Discipline discipline = Discipline.valueOf(event.discipline().toUpperCase(Locale.ROOT));
        FetchObdxClassificationDTO obdx = discipline == Discipline.OBDX
                ? getObdxClassificationServiceCase.getClassification(event)
                : null;

        return new FetchClassificationDTO(eventId, event.name(), event.stageId(), context.stageName(),
                event.configurationId(), obdx);
    }

    private EventClassificationContextDTO resolveContext(String eventId) {
        EventClassificationContextDTO cached =
                eventClassificationCacheManagerPort.getIfPresentAndValid(eventId, EVENT_CONTEXT_TTL_SECONDS);
        if (cached != null) {
            return cached;
        }

        Event event = getEventPersistencePort.getEvent(eventId);
        if (event == null) throw new EventNotFoundException();
        if (event.deletedAt() != null) throw new EventAlreadyDeletedException();

        Stage stage = getStagePersistencePort.getStage(event.stageId());

        EventClassificationContextDTO context = new EventClassificationContextDTO(event, stage.name());
        eventClassificationCacheManagerPort.put(eventId, context);
        return context;
    }
}
