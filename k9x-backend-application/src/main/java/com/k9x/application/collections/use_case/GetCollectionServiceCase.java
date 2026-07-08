package com.k9x.application.collections.use_case;

import com.k9x.application.collections.obdx.port.GetObdxCollectionEventJudgesPersistencePort;
import com.k9x.application.collections.obdx.use_case.GetObdxCollectionServiceCase;
import com.k9x.application.collections.obdx.use_case.dto.FetchObdxCollectionDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionDetailDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionJudgeWithCollectorDTO;
import com.k9x.application.competitions.CompetitionNavigator;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.disciplines.obdx.port.GetObdxConfigurationAllowedValuesPort;
import com.k9x.domain.events.exceptions.EventAlreadyDeletedException;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.exceptions.ObdxUserNotCollectorException;
import com.k9x.domain.stages.exceptions.StageExpiredException;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.disciplines.valueobjects.Discipline;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.shared.UtcDates;
import com.k9x.domain.stages.aggregates.StageSnapshot;

import java.util.List;

public class GetCollectionServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final GetObdxCollectionEventJudgesPersistencePort getObdxCollectionEventJudgesPersistencePort;
    private final GetObdxConfigurationAllowedValuesPort getObdxConfigurationAllowedValuesPort;
    private final GetObdxCollectionServiceCase getObdxCollectionServiceCase;

    public GetCollectionServiceCase(
            GetCompetitionPersistencePort getCompetitionPersistencePort,
            GetObdxCollectionEventJudgesPersistencePort getObdxCollectionEventJudgesPersistencePort,
            GetObdxConfigurationAllowedValuesPort getObdxConfigurationAllowedValuesPort,
            GetObdxCollectionServiceCase getObdxCollectionServiceCase) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.getObdxCollectionEventJudgesPersistencePort = getObdxCollectionEventJudgesPersistencePort;
        this.getObdxConfigurationAllowedValuesPort = getObdxConfigurationAllowedValuesPort;
        this.getObdxCollectionServiceCase = getObdxCollectionServiceCase;
    }

    public FetchCollectionDetailDTO getCollection(String eventId, String userId) {
        String competitionId = getCompetitionPersistencePort.competitionIdByEvent(eventId);
        if (competitionId == null) throw new EventNotFoundException();
        CompetitionSnapshot competition = getCompetitionPersistencePort.getCompetition(competitionId);
        EventSnapshot event = CompetitionNavigator.findEvent(competition, eventId);
        assertEventValidations(event);
        StageSnapshot stage = CompetitionNavigator.findStageOfEvent(competition, eventId);
        if (!event.creator().equals(userId)) {
            assertStageNotExpired(stage);
        }

        List<FetchCollectionJudgeWithCollectorDTO> allJudges =
                getObdxCollectionEventJudgesPersistencePort.getJudges(eventId);
        List<FetchCollectionJudgeWithCollectorDTO> visibleJudges =
                resolveVisibleJudges(allJudges, event.creator(), userId);

        Discipline discipline = Discipline.fromStored(event.discipline());
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

    private void assertEventValidations(EventSnapshot event) {
        if (event == null) throw new EventNotFoundException();
        if (event.deletedAt() != null) throw new EventAlreadyDeletedException();
    }

    private void assertStageNotExpired(StageSnapshot stage) {
        if (UtcDates.isAfterUtcDay(DateUtils.nowUtcMillis(), stage.dateTo())) throw new StageExpiredException();
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
