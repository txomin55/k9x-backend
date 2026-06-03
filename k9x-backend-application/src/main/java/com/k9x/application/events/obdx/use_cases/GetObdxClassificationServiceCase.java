package com.k9x.application.events.obdx.use_cases;

import com.k9x.application.events.obdx.port.GetClassificationPersistencePort;
import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.use_cases.dto.*;
import com.k9x.application.events.obdx.use_cases.port.ClassificationCacheManagerPort;
import com.k9x.domain.aggregates.disciplines.Discipline;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.aggregates.events.ClassificationCompetitorStatus;
import com.k9x.domain.aggregates.events.Event;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class GetObdxClassificationServiceCase {

    private final GetObdxClassificationConfigPort getObdxClassificationConfigPort;
    private final ClassificationCacheManagerPort classificationCacheManagerPort;
    private final GetClassificationPersistencePort getClassificationPersistencePort;

    public GetObdxClassificationServiceCase(
            GetObdxClassificationConfigPort getObdxClassificationConfigPort,
            ClassificationCacheManagerPort classificationCacheManagerPort,
            GetClassificationPersistencePort getClassificationPersistencePort) {
        this.getObdxClassificationConfigPort = getObdxClassificationConfigPort;
        this.classificationCacheManagerPort = classificationCacheManagerPort;
        this.getClassificationPersistencePort = getClassificationPersistencePort;
    }

    public Discipline discipline() {
        return Discipline.OBDX;
    }

    public FetchObdxClassificationDTO getClassification(Event event) {
        String eventId = event.id();
        ObdxClassificationConfigDTO config = getObdxClassificationConfigPort.getConfig(event.configurationId());

        FetchObdxClassificationDTO cached = classificationCacheManagerPort.getIfPresentAndValid(eventId, config.cacheEvictStrategy().getTtlSeconds());
        if (cached != null) {
            return cached;
        }

        List<FetchClassificationRawRowDTO> rawRows = getClassificationPersistencePort.getClassification(eventId);

        FetchObdxClassificationDTO dto = aggregateProjection(event, config, rawRows);
        classificationCacheManagerPort.put(eventId, dto);
        return dto;
    }

    private FetchObdxClassificationDTO aggregateProjection(Event event,
                                                           ObdxClassificationConfigDTO config,
                                                           List<FetchClassificationRawRowDTO> rawRows) {
        // dogId → exerciseId → list of (judgeId, judgeName, score)
        Map<String, Map<String, List<FetchClassificationJudgeScoreDTO>>> judgeScoresByDogExercise = new LinkedHashMap<>();
        // dog metadata keyed by dogId (first row wins)
        Map<String, FetchClassificationRawRowDTO> dogMeta = new LinkedHashMap<>();
        // exerciseId → position and tags (same for all dogs)
        Map<String, Short> exercisePositions = new LinkedHashMap<>();
        Map<String, List<String>> exerciseTags = new LinkedHashMap<>();

        Long scoresLastUpdate = null;

        for (FetchClassificationRawRowDTO row : rawRows) {
            dogMeta.putIfAbsent(row.dogId(), row);
            exercisePositions.putIfAbsent(row.exerciseId(), row.exercisePosition());
            exerciseTags.putIfAbsent(row.exerciseId(),
                    row.exerciseTags() != null ? Arrays.asList(row.exerciseTags()) : List.of());

            judgeScoresByDogExercise
                    .computeIfAbsent(row.dogId(), _ -> new LinkedHashMap<>())
                    .computeIfAbsent(row.exerciseId(), _ -> new ArrayList<>());

            if (row.score() != null) {
                BigDecimal judgeScoreRating = percentageOfMax(row.score(), config.maxAllowedScore());
                List<FetchClassificationJudgeScoreDTO> exerciseScores = judgeScoresByDogExercise.get(row.dogId()).get(row.exerciseId());
                exerciseScores.add(new FetchClassificationJudgeScoreDTO(row.judgeId(), row.judgeName(), row.score(), judgeScoreRating));
                scoresLastUpdate = scoresLastUpdate == null
                        ? row.scoreLastUpdate() : Math.max(scoresLastUpdate, row.scoreLastUpdate());
            }
        }

        List<FetchClassificationCompetitorDTO> competitors = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<FetchClassificationJudgeScoreDTO>>> dogEntry
                : judgeScoresByDogExercise.entrySet()) {
            String dogId = dogEntry.getKey();
            FetchClassificationRawRowDTO meta = dogMeta.get(dogId);
            BigDecimal totalScore = BigDecimal.ZERO;
            List<FetchClassificationExerciseScoreDTO> exercises = new ArrayList<>();

            for (Map.Entry<String, List<FetchClassificationJudgeScoreDTO>> exEntry : dogEntry.getValue().entrySet()) {
                String exerciseId = exEntry.getKey();
                List<FetchClassificationJudgeScoreDTO> judgeEntries = exEntry.getValue();
                List<BigDecimal> scores = judgeEntries.stream().map(FetchClassificationJudgeScoreDTO::score).toList();

                BigDecimal coef = config.coefByExerciseId().getOrDefault(exerciseId, BigDecimal.ONE);
                BigDecimal rawScore = computeAvg(scores, event.scoreCalculation());
                BigDecimal weightedScore = rawScore.multiply(coef).setScale(2, RoundingMode.HALF_UP);
                BigDecimal maxExerciseScore = config.maxAllowedScore().multiply(coef);
                BigDecimal exerciseScoreRating = percentageOfMax(weightedScore, maxExerciseScore);

                totalScore = totalScore.add(weightedScore);
                exercises.add(new FetchClassificationExerciseScoreDTO(
                        exerciseId,
                        exercisePositions.getOrDefault(exerciseId, (short) 0),
                        exerciseTags.getOrDefault(exerciseId, List.of()),
                        rawScore, weightedScore, exerciseScoreRating,
                        judgeEntries));
            }

            BigDecimal maxPossibleTotal = computeMaxPossibleTotal(config);
            BigDecimal competitorScoreRating = percentageOfMax(totalScore, maxPossibleTotal);

            competitors.add(new FetchClassificationCompetitorDTO(
                    dogId, meta.dogName(), meta.dogOwner(), meta.dogTeam(), meta.dogCountry(),
                    0, totalScore, competitorScoreRating, false, ClassificationCompetitorStatus.LIVE.name(), exercises));
        }

        assignPositions(competitors, config);

        return new FetchObdxClassificationDTO(scoresLastUpdate, competitors);
    }

    private BigDecimal computeMaxPossibleTotal(ObdxClassificationConfigDTO config) {
        return config.coefByExerciseId().values().stream()
                .map(coef -> config.maxAllowedScore().multiply(coef))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal percentageOfMax(BigDecimal score, BigDecimal max) {
        if (max.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return score.divide(max, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeAvg(List<BigDecimal> scores, ObdxAvgMethod method) {
        if (scores.isEmpty()) return BigDecimal.ZERO;
        if (method == ObdxAvgMethod.MID_AVG && scores.size() >= 4) {
            List<BigDecimal> trimmed = new ArrayList<>(scores);
            trimmed.remove(Collections.min(trimmed));
            trimmed.remove(Collections.max(trimmed));
            return average(trimmed);
        }
        return average(scores);
    }

    private BigDecimal average(List<BigDecimal> values) {
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
    }

    private void assignPositions(List<FetchClassificationCompetitorDTO> competitors,
                                 ObdxClassificationConfigDTO config) {
        Comparator<FetchClassificationCompetitorDTO> comparator =
                Comparator.comparing(FetchClassificationCompetitorDTO::totalScore).reversed()
                        .thenComparing(c -> tieScore(c, config.breakTie()), Comparator.reverseOrder())
                        .thenComparing(c -> tieScore(c, config.breakTieTie()), Comparator.reverseOrder());

        competitors.sort(comparator);

        int position = 1;
        for (int i = 0; i < competitors.size(); i++) {
            if (i > 0 && isTied(competitors.get(i - 1), competitors.get(i), config)) {
                setPosition(competitors, i, competitors.get(i - 1).position(), true);
            } else {
                setPosition(competitors, i, position, false);
            }
            position++;
        }
    }

    private BigDecimal tieScore(FetchClassificationCompetitorDTO competitor, List<String> exerciseIds) {
        if (exerciseIds == null || exerciseIds.isEmpty()) return BigDecimal.ZERO;
        return competitor.exercises().stream()
                .filter(e -> exerciseIds.contains(e.exerciseId()))
                .map(FetchClassificationExerciseScoreDTO::totalScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isTied(FetchClassificationCompetitorDTO a, FetchClassificationCompetitorDTO b,
                           ObdxClassificationConfigDTO config) {
        return a.totalScore().compareTo(b.totalScore()) == 0
                && tieScore(a, config.breakTie()).compareTo(tieScore(b, config.breakTie())) == 0
                && tieScore(a, config.breakTieTie()).compareTo(tieScore(b, config.breakTieTie())) == 0;
    }

    private void setPosition(List<FetchClassificationCompetitorDTO> competitors, int index, int position, boolean tied) {
        FetchClassificationCompetitorDTO c = competitors.get(index);
        competitors.set(index, new FetchClassificationCompetitorDTO(
                c.dogId(), c.dogName(), c.owner(), c.team(), c.country(),
                position, c.totalScore(), c.scoreRating(), tied, c.status(), c.exercises()));
    }
}
