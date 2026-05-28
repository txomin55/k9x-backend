package com.k9x.application.collections.use_case;

import com.k9x.application.collections.port.GetCollectionCompetitorsPersistencePort;
import com.k9x.application.collections.port.GetCollectionEventJudgesPersistencePort;
import com.k9x.application.collections.port.GetCollectionExercisesPersistencePort;
import com.k9x.application.collections.port.GetCollectionScoresPersistencePort;
import com.k9x.application.collections.use_case.dto.FetchCollectionCompetitorDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionDetailDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionExerciseDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionJudgeWithCollectorDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionScoreDTO;
import com.k9x.application.disciplines.obdx.port.GetObdxConfigurationAllowedValuesPort;
import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.exceptions.UserNotCollectorException;
import com.k9x.application.events.obdx.port.GetObdxEventPersistencePort;
import com.k9x.application.stages.exceptions.StageExpiredException;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.events.obdx.EventCompetitorStatus;
import com.k9x.domain.aggregates.events.obdx.ObdxEvent;
import com.k9x.domain.aggregates.stages.Stage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GetCollectionServiceCase {

    private final GetObdxEventPersistencePort getObdxEventPersistencePort;
    private final GetStagePersistencePort getStagePersistencePort;
    private final GetCollectionEventJudgesPersistencePort getCollectionEventJudgesPersistencePort;
    private final GetCollectionCompetitorsPersistencePort getCollectionCompetitorsPersistencePort;
    private final GetCollectionExercisesPersistencePort getCollectionExercisesPersistencePort;
    private final GetCollectionScoresPersistencePort getCollectionScoresPersistencePort;
    private final GetObdxConfigurationAllowedValuesPort getObdxConfigurationAllowedValuesPort;

    public GetCollectionServiceCase(
            GetObdxEventPersistencePort getObdxEventPersistencePort,
            GetStagePersistencePort getStagePersistencePort,
            GetCollectionEventJudgesPersistencePort getCollectionEventJudgesPersistencePort,
            GetCollectionCompetitorsPersistencePort getCollectionCompetitorsPersistencePort,
            GetCollectionExercisesPersistencePort getCollectionExercisesPersistencePort,
            GetCollectionScoresPersistencePort getCollectionScoresPersistencePort,
            GetObdxConfigurationAllowedValuesPort getObdxConfigurationAllowedValuesPort) {
        this.getObdxEventPersistencePort = getObdxEventPersistencePort;
        this.getStagePersistencePort = getStagePersistencePort;
        this.getCollectionEventJudgesPersistencePort = getCollectionEventJudgesPersistencePort;
        this.getCollectionCompetitorsPersistencePort = getCollectionCompetitorsPersistencePort;
        this.getCollectionExercisesPersistencePort = getCollectionExercisesPersistencePort;
        this.getCollectionScoresPersistencePort = getCollectionScoresPersistencePort;
        this.getObdxConfigurationAllowedValuesPort = getObdxConfigurationAllowedValuesPort;
    }

    public FetchCollectionDetailDTO getCollection(String eventId, String userId) {
        ObdxEvent event = getObdxEventPersistencePort.getEvent(eventId);
        assertEventValidations(event);
        Stage stage = getStagePersistencePort.getStage(event.stageId());
        assertStageNotExpired(stage);

        List<FetchCollectionJudgeWithCollectorDTO> allJudges =
                getCollectionEventJudgesPersistencePort.getJudges(eventId);
        List<FetchCollectionJudgeWithCollectorDTO> visibleJudges =
                resolveVisibleJudges(allJudges, event.creator(), userId);

        Set<String> visibleJudgeIds = visibleJudges.stream()
                .map(FetchCollectionJudgeWithCollectorDTO::judgeId)
                .collect(Collectors.toSet());

        List<FetchCollectionCompetitorDTO> competitors =
                getCollectionCompetitorsPersistencePort.getCompetitors(eventId).stream()
                        .map(c -> new FetchCollectionCompetitorDTO(c.dogId(), c.dogName(), c.dogIdentity(),
                                c.owner(), c.team(), c.country(), c.position(), c.verified(),
                                EventCompetitorStatus.ENROLLED.name()))
                        .toList();
        List<FetchCollectionExerciseDTO> exercises =
                getCollectionExercisesPersistencePort.getExercises(eventId);
        List<FetchCollectionScoreDTO> scores = getCollectionScoresPersistencePort.getScores(eventId).stream()
                .filter(s -> visibleJudgeIds.contains(s.judgeId()))
                .toList();

        return new FetchCollectionDetailDTO(
                event.configurationId(),
                getObdxConfigurationAllowedValuesPort.getAllowedValues(event.configurationId()),
                visibleJudges,
                competitors,
                exercises,
                scores
        );
    }

    private void assertEventValidations(ObdxEvent event) {
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
        if (collectorJudges.isEmpty()) throw new UserNotCollectorException();
        return collectorJudges;
    }
}
