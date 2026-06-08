package com.k9x.application.collections.use_case;

import com.k9x.application.collections.obdx.port.GetObdxCollectionEventJudgesPersistencePort;
import com.k9x.application.collections.obdx.use_case.GetObdxCollectionServiceCase;
import com.k9x.application.collections.obdx.use_case.dto.FetchObdxCollectionDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionDetailDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionJudgeWithCollectorDTO;
import com.k9x.application.competitions.CompetitionNavigator;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.disciplines.obdx.port.GetObdxConfigurationAllowedValuesPort;
import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.exceptions.ObdxUserNotCollectorException;
import com.k9x.application.stages.exceptions.StageExpiredException;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.aggregates.disciplines.Discipline;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.stages.Stage;

import java.util.List;
import java.util.Locale;

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
        Competition competition = getCompetitionPersistencePort.getCompetition(competitionId);
        Event event = CompetitionNavigator.findEvent(competition, eventId);
        assertEventValidations(event);
        Stage stage = CompetitionNavigator.findStageOfEvent(competition, eventId);
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
