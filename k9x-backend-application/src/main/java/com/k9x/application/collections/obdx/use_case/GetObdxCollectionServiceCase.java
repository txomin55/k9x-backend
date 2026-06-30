package com.k9x.application.collections.obdx.use_case;

import com.k9x.application.collections.obdx.port.GetObdxCollectionCompetitorsPersistencePort;
import com.k9x.application.collections.obdx.port.GetObdxCollectionExercisesPersistencePort;
import com.k9x.application.collections.obdx.port.GetObdxCollectionScoresPersistencePort;
import com.k9x.application.collections.obdx.use_case.dto.FetchCollectionCompetitorScoresDTO;
import com.k9x.application.collections.obdx.use_case.dto.FetchCollectionExerciseScoresDTO;
import com.k9x.application.collections.obdx.use_case.dto.FetchCollectionJudgeScoreDTO;
import com.k9x.application.collections.obdx.use_case.dto.FetchObdxCollectionDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionCompetitorDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionExerciseDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionJudgeWithCollectorDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionScoreDTO;
import com.k9x.domain.disciplines.valueobjects.Discipline;
import com.k9x.domain.events.status.EventCompetitorStatus;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GetObdxCollectionServiceCase {

    private final GetObdxCollectionCompetitorsPersistencePort getObdxCollectionCompetitorsPersistencePort;
    private final GetObdxCollectionExercisesPersistencePort getObdxCollectionExercisesPersistencePort;
    private final GetObdxCollectionScoresPersistencePort getObdxCollectionScoresPersistencePort;

    public GetObdxCollectionServiceCase(
            GetObdxCollectionCompetitorsPersistencePort getObdxCollectionCompetitorsPersistencePort,
            GetObdxCollectionExercisesPersistencePort getObdxCollectionExercisesPersistencePort,
            GetObdxCollectionScoresPersistencePort getObdxCollectionScoresPersistencePort) {
        this.getObdxCollectionCompetitorsPersistencePort = getObdxCollectionCompetitorsPersistencePort;
        this.getObdxCollectionExercisesPersistencePort = getObdxCollectionExercisesPersistencePort;
        this.getObdxCollectionScoresPersistencePort = getObdxCollectionScoresPersistencePort;
    }

    public Discipline discipline() {
        return Discipline.OBDX;
    }

    public FetchObdxCollectionDTO getCollection(
            String eventId, List<FetchCollectionJudgeWithCollectorDTO> visibleJudges) {
        Set<String> visibleJudgeIds = visibleJudges.stream()
                .map(FetchCollectionJudgeWithCollectorDTO::judgeId)
                .collect(Collectors.toSet());

        List<FetchCollectionCompetitorDTO> competitors =
                getObdxCollectionCompetitorsPersistencePort.getCompetitors(eventId).stream()
                        .map(c -> new FetchCollectionCompetitorDTO(c.dogId(), c.dogName(), c.dogIdentity(),
                                c.breed(), c.owner(), c.handler(), c.team(), c.country(), c.position(), c.verified(),
                                c.notCompeting(), EventCompetitorStatus.of(c.notCompeting()).name()))
                        .toList();
        List<FetchCollectionExerciseDTO> exercises =
                getObdxCollectionExercisesPersistencePort.getExercises(eventId);
        List<FetchCollectionScoreDTO> scores = getObdxCollectionScoresPersistencePort.getScores(eventId).stream()
                .filter(s -> visibleJudgeIds.contains(s.judgeId()))
                .toList();

        List<FetchCollectionCompetitorScoresDTO> competitorScores = competitors.stream()
                .map(comp -> new FetchCollectionCompetitorScoresDTO(
                        comp,
                        exercises.stream()
                                .map(ex -> new FetchCollectionExerciseScoresDTO(
                                        ex.exerciseId(),
                                        ex.position(),
                                        visibleJudges.stream()
                                                .map(judge -> new FetchCollectionJudgeScoreDTO(
                                                        scores.stream()
                                                                .filter(s -> s.dogId().equals(comp.dogId())
                                                                        && s.exerciseId().equals(ex.exerciseId())
                                                                        && s.judgeId().equals(judge.judgeId()))
                                                                .findFirst()
                                                                .map(FetchCollectionScoreDTO::score)
                                                                .orElse(null),
                                                        judge.judgeId(), judge.judgeName()))
                                                .toList()))
                                .toList()))
                .toList();

        return new FetchObdxCollectionDTO(competitorScores);
    }
}
