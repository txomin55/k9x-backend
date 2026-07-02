package com.k9x.application.events.use_case;

import com.k9x.application.competitions.CompetitionNavigator;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.domain.events.exceptions.EventAlreadyDeletedException;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.use_case.GetObdxClassificationServiceCase;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.use_case.dto.EventClassificationContextDTO;
import com.k9x.application.events.use_case.port.EventClassificationCacheManagerPort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.disciplines.valueobjects.Discipline;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.stages.aggregates.StageSnapshot;

import java.util.Locale;

public class GetEventClassificationServiceCase {

    private static final int EVENT_CONTEXT_TTL_SECONDS = 30;

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final EventClassificationCacheManagerPort eventClassificationCacheManagerPort;
    private final GetObdxClassificationServiceCase getObdxClassificationServiceCase;

    public GetEventClassificationServiceCase(
            GetCompetitionPersistencePort getCompetitionPersistencePort,
            EventClassificationCacheManagerPort eventClassificationCacheManagerPort,
            GetObdxClassificationServiceCase getObdxClassificationServiceCase) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.eventClassificationCacheManagerPort = eventClassificationCacheManagerPort;
        this.getObdxClassificationServiceCase = getObdxClassificationServiceCase;
    }

    public FetchClassificationDTO getClassification(String eventId) {
        EventClassificationContextDTO context = resolveContext(eventId);
        EventSnapshot event = context.event();

        Discipline discipline = Discipline.valueOf(event.discipline().toUpperCase(Locale.ROOT));
        FetchObdxClassificationDTO obdx = discipline == Discipline.OBDX
                ? getObdxClassificationServiceCase.getClassification(event)
                : null;

        Long scoresLastUpdate = obdx == null ? null : obdx.scoresLastUpdate();

        long now = DateUtils.nowUtcMillis();
        return new FetchClassificationDTO(eventId, event.name(), event.status(now, context.stageDateTo()).name(),
                event.stageId(), context.stageName(), event.configurationId(), scoresLastUpdate, obdx);
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

        EventClassificationContextDTO context = new EventClassificationContextDTO(event, stage.name(), stage.dateTo());
        eventClassificationCacheManagerPort.put(eventId, context);
        return context;
    }
}
