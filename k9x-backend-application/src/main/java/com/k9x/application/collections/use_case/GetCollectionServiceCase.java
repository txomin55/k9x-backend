package com.k9x.application.collections.use_case;

import com.k9x.application.collections.obdx.port.GetObdxCollectionEventJudgesPersistencePort;
import com.k9x.application.collections.obdx.use_case.GetObdxCollectionServiceCase;
import com.k9x.application.collections.obdx.use_case.dto.FetchObdxCollectionDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionDetailDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionJudgeWithCollectorDTO;
import com.k9x.application.disciplines.obdx.port.GetObdxConfigurationAllowedValuesPort;
import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.exceptions.ObdxUserNotCollectorException;
import com.k9x.application.events.obdx.use_case.port.GetEventPersistencePort;
import com.k9x.application.stages.exceptions.StageExpiredException;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.disciplines.Discipline;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.stages.Stage;

import java.util.List;
import java.util.Locale;

public class GetCollectionServiceCase {

    private final GetEventPersistencePort getEventPersistencePort;
    private final GetStagePersistencePort getStagePersistencePort;
    private final GetObdxCollectionEventJudgesPersistencePort getObdxCollectionEventJudgesPersistencePort;
    private final GetObdxConfigurationAllowedValuesPort getObdxConfigurationAllowedValuesPort;
    private final GetObdxCollectionServiceCase getObdxCollectionServiceCase;

    public GetCollectionServiceCase(
            GetEventPersistencePort getEventPersistencePort,
            GetStagePersistencePort getStagePersistencePort,
            GetObdxCollectionEventJudgesPersistencePort getObdxCollectionEventJudgesPersistencePort,
            GetObdxConfigurationAllowedValuesPort getObdxConfigurationAllowedValuesPort,
            GetObdxCollectionServiceCase getObdxCollectionServiceCase) {
        this.getEventPersistencePort = getEventPersistencePort;
        this.getStagePersistencePort = getStagePersistencePort;
        this.getObdxCollectionEventJudgesPersistencePort = getObdxCollectionEventJudgesPersistencePort;
        this.getObdxConfigurationAllowedValuesPort = getObdxConfigurationAllowedValuesPort;
        this.getObdxCollectionServiceCase = getObdxCollectionServiceCase;
    }

    public FetchCollectionDetailDTO getCollection(String eventId, String userId) {
        Event event = getEventPersistencePort.getEvent(eventId);
        assertEventValidations(event);
        Stage stage = getStagePersistencePort.getStage(event.stageId());
        assertStageNotExpired(stage);

        List<FetchCollectionJudgeWithCollectorDTO> allJudges =
                getObdxCollectionEventJudgesPersistencePort.getJudges(eventId);
        List<FetchCollectionJudgeWithCollectorDTO> visibleJudges =
                resolveVisibleJudges(allJudges, event.creator(), userId);

        Discipline discipline = Discipline.valueOf(event.discipline().toUpperCase(Locale.ROOT));
        FetchObdxCollectionDTO obdx = discipline == Discipline.OBDX
                ? getObdxCollectionServiceCase.getCollection(eventId, visibleJudges)
                : null;

        return new FetchCollectionDetailDTO(
                event.configurationId(),
                event.discipline(),
                getObdxConfigurationAllowedValuesPort.getAllowedValues(event.configurationId()),
                obdx
        );
    }

    private void assertEventValidations(Event event) {
        if (event == null) throw new EventNotFoundException();
        if (event.deletedAt() != null) throw new EventAlreadyDeletedException();
    }

    private void assertStageNotExpired(Stage stage) {
        if (stage.dateTo() < DateUtils.nowUtcMillis()) throw new StageExpiredException();
    }

    private List<FetchCollectionJudgeWithCollectorDTO> resolveVisibleJudges(
            List<FetchCollectionJudgeWithCollectorDTO> allJudges, String creator, String userId) {
        if (creator.equals(userId)) return allJudges;
        List<FetchCollectionJudgeWithCollectorDTO> collectorJudges = allJudges.stream()
                .filter(j -> userId.equals(j.collectorEmail()))
                .toList();
        if (collectorJudges.isEmpty()) throw new ObdxUserNotCollectorException();
        return collectorJudges;
    }
}
