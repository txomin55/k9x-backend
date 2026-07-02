package com.k9x.application.events.obdx.use_case;

import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.use_case.dto.*;
import com.k9x.application.events.obdx.use_case.port.ClassificationCacheManagerPort;
import com.k9x.domain.disciplines.valueobjects.Discipline;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.events.status.ClassificationCompetitorStatus;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.valueobjects.EventCompetitor;
import com.k9x.domain.events.valueobjects.EventExercise;
import com.k9x.domain.events.valueobjects.EventJudge;
import com.k9x.domain.events.valueobjects.Score;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class GetObdxClassificationServiceCase {

    private final GetObdxClassificationConfigPort getObdxClassificationConfigPort;
    private final ClassificationCacheManagerPort classificationCacheManagerPort;

    private static final BigDecimal YELLOW_CARD_PENALTY = BigDecimal.TEN;

    public GetObdxClassificationServiceCase(
            GetObdxClassificationConfigPort getObdxClassificationConfigPort,
            ClassificationCacheManagerPort classificationCacheManagerPort) {
        this.getObdxClassificationConfigPort = getObdxClassificationConfigPort;
        this.classificationCacheManagerPort = classificationCacheManagerPort;
    }

    public Discipline discipline() {
        return Discipline.OBDX;
    }

    public FetchObdxClassificationDTO getClassification(EventSnapshot event) {
        String eventId = event.id();
        ObdxClassificationConfigDTO config = getObdxClassificationConfigPort.getConfig(event.configurationId());

        FetchObdxClassificationDTO cached = classificationCacheManagerPort.getIfPresentAndValid(eventId, config.cacheEvictStrategy().getTtlSeconds());
        if (cached != null) {
            return cached;
        }

        List<FetchClassificationRawRowDTO> rawRows = buildRawRows(event);

        FetchObdxClassificationDTO dto = aggregateProjection(event, config, rawRows);
        classificationCacheManagerPort.put(eventId, dto);
        return dto;
    }

    private List<FetchClassificationRawRowDTO> buildRawRows(EventSnapshot event) {
        List<EventCompetitor> competitors = event.competitors() == null ? List.of() : event.competitors();
        List<EventExercise> exercises = event.exercises() == null ? List.of() : event.exercises();
        List<EventJudge> judges = event.judges() == null ? List.of() : event.judges();
        List<Score> scores = event.scores() == null ? List.of() : event.scores();

        List<EventExercise> sortedExercises = new ArrayList<>(exercises);
        sortedExercises.sort(Comparator.comparing(
                e -> e.position() == null ? Short.MAX_VALUE : e.position()));

        List<FetchClassificationRawRowDTO> rows = new ArrayList<>();
        for (EventCompetitor c : competitors) {
            for (EventExercise ex : sortedExercises) {
                for (EventJudge jd : judges) {
                    Score s = scores.stream()
                            .filter(sc -> Objects.equals(sc.exerciseId(), ex.exerciseId())
                                    && Objects.equals(sc.judgeId(), jd.judgeId())
                                    && Objects.equals(sc.dogId(), c.dogId()))
                            .findFirst().orElse(null);
                    rows.add(new FetchClassificationRawRowDTO(
                            c.dogId(), c.dogName(), c.owner(), c.handler(), c.team(), c.country(),
                            ex.exerciseId(), ex.position() == null ? (short) 0 : ex.position(),
                            ex.tags() == null ? null : ex.tags().toArray(new String[0]),
                            jd.judgeId(), jd.judgeName(),
                            s == null ? null : s.score(),
                            s == null ? null : s.lastUpdate(),
                            s == null ? null : s.yellowCard()));
                }
            }
        }
        return rows;
    }

    private FetchObdxClassificationDTO aggregateProjection(EventSnapshot event,
                                                           ObdxClassificationConfigDTO config,
                                                           List<FetchClassificationRawRowDTO> rawRows) {
        // dogId → exerciseId → list of (judgeId, judgeName, score)
        Map<String, Map<String, List<FetchClassificationJudgeScoreDTO>>> judgeScoresByDogExercise = new LinkedHashMap<>();
        // dogId → exerciseId → list of yellow cards stamped for that exercise (one per stamped slot, across judges)
        Map<String, Map<String, List<FetchClassificationYellowCardDTO>>> yellowCardsByDogExercise = new LinkedHashMap<>();
        // dog metadata keyed by dogId (first row wins)
        Map<String, FetchClassificationRawRowDTO> dogMeta = new LinkedHashMap<>();
        // exerciseId → position and tags (same for all dogs)
        Map<String, Short> exercisePositions = new LinkedHashMap<>();
        Map<String, List<String>> exerciseTags = new LinkedHashMap<>();
        // dogId → static start order (competitor number), set on enrollment
        Map<String, Short> startOrderByDog = new LinkedHashMap<>();
        // dogId → manually set final score; when present it overrides the computed totalScore
        Map<String, BigDecimal> finalScoreByDog = new LinkedHashMap<>();
        for (EventCompetitor competitor : (event.competitors() == null ? List.<EventCompetitor>of() : event.competitors())) {
            startOrderByDog.put(competitor.dogId(), competitor.position());
            finalScoreByDog.put(competitor.dogId(), competitor.finalScore());
        }

        Long scoresLastUpdate = null;

        for (FetchClassificationRawRowDTO row : rawRows) {
            dogMeta.putIfAbsent(row.dogId(), row);
            exercisePositions.putIfAbsent(row.exerciseId(), row.exercisePosition());
            exerciseTags.putIfAbsent(row.exerciseId(),
                    row.exerciseTags() != null ? Arrays.asList(row.exerciseTags()) : List.of());

            judgeScoresByDogExercise
                    .computeIfAbsent(row.dogId(), _ -> new LinkedHashMap<>())
                    .computeIfAbsent(row.exerciseId(), _ -> new ArrayList<>());
            List<FetchClassificationYellowCardDTO> exerciseYellowCards = yellowCardsByDogExercise
                    .computeIfAbsent(row.dogId(), _ -> new LinkedHashMap<>())
                    .computeIfAbsent(row.exerciseId(), _ -> new ArrayList<>());

            if (row.yellowCard() != null) {
                exerciseYellowCards.add(
                        new FetchClassificationYellowCardDTO(row.judgeId(), row.judgeName(), row.yellowCard()));
            }

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
            BigDecimal computedTotal = BigDecimal.ZERO;
            List<FetchClassificationExerciseScoreDTO> exercises = new ArrayList<>();

            for (Map.Entry<String, List<FetchClassificationJudgeScoreDTO>> exEntry : dogEntry.getValue().entrySet()) {
                String exerciseId = exEntry.getKey();
                List<FetchClassificationJudgeScoreDTO> judgeEntries = exEntry.getValue();
                List<BigDecimal> scores = judgeEntries.stream().map(FetchClassificationJudgeScoreDTO::score).toList();

                BigDecimal coef = config.coefByExerciseId().getOrDefault(exerciseId, BigDecimal.ONE);
                List<FetchClassificationYellowCardDTO> exerciseYellowCards = yellowCardsByDogExercise
                        .getOrDefault(dogId, Map.of()).getOrDefault(exerciseId, List.of());
                // exerciseScore is the maximum attainable for this exercise (highest allowed score * coef); it is a
                // constant reference. totalScore is what the competitor has actually achieved: the judges' average
                // (or mid-avg) times the coef, which is 0 while the exercise has no scores, minus a flat penalty
                // if a yellow card was stamped for this exercise (never below zero).
                BigDecimal maxExerciseScore = config.maxAllowedScore().multiply(coef).setScale(2, RoundingMode.HALF_UP);
                BigDecimal weightedScore = scores.isEmpty() ? null
                        : computeAvg(scores, event.scoreCalculation())
                        .multiply(coef).setScale(2, RoundingMode.HALF_UP);
                if (weightedScore != null && !exerciseYellowCards.isEmpty()) {
                    weightedScore = weightedScore.subtract(YELLOW_CARD_PENALTY).max(BigDecimal.ZERO);
                }
                BigDecimal exerciseScoreRating = weightedScore == null ? null
                        : percentageOfMax(weightedScore, maxExerciseScore);

                if (weightedScore != null) {
                    computedTotal = computedTotal.add(weightedScore);
                }
                exercises.add(new FetchClassificationExerciseScoreDTO(
                        exerciseId,
                        exercisePositions.getOrDefault(exerciseId, (short) 0),
                        exerciseTags.getOrDefault(exerciseId, List.of()),
                        maxExerciseScore, weightedScore, exerciseScoreRating,
                        judgeEntries, exerciseYellowCards));
            }

            // A manually set final score takes precedence over the computed sum of exercise scores.
            BigDecimal manualFinalScore = finalScoreByDog.get(dogId);
            BigDecimal totalScore = manualFinalScore != null
                    ? manualFinalScore.setScale(2, RoundingMode.HALF_UP) : computedTotal;

            BigDecimal maxPossibleTotal = computeMaxPossibleTotal(config, exercisePositions.keySet());
            BigDecimal competitorScoreRating = percentageOfMax(totalScore, maxPossibleTotal);

            ClassificationCompetitorStatus status;
            if (event.isCompetitorSettled(dogId)) {
                status = ClassificationCompetitorStatus.SETTLED;
            } else if (event.isCompetitorStarted(dogId)) {
                status = ClassificationCompetitorStatus.LIVE;
            } else {
                status = ClassificationCompetitorStatus.PENDING;
            }

            competitors.add(new FetchClassificationCompetitorDTO(
                    dogId, meta.dogName(), meta.dogOwner(), meta.dogHandler(), meta.dogTeam(), meta.dogCountry(),
                    startOrderByDog.get(dogId), 0, totalScore, competitorScoreRating, false,
                    status.name(), exercises));
        }

        assignPositions(competitors, config);

        return new FetchObdxClassificationDTO(scoresLastUpdate, competitors);
    }

    /**
     * Maximum attainable total for the competitor: summed over the exercises that actually belong to the event
     * (not every exercise defined in the federation config), so it matches the totalScore numerator and yields a
     * 0-100 rating.
     */
    private BigDecimal computeMaxPossibleTotal(ObdxClassificationConfigDTO config, Collection<String> eventExerciseIds) {
        return eventExerciseIds.stream()
                .map(id -> config.maxAllowedScore().multiply(
                        config.coefByExerciseId().getOrDefault(id, BigDecimal.ONE)))
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
                .filter(Objects::nonNull)
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
                c.dogId(), c.dogName(), c.owner(), c.handler(), c.team(), c.country(),
                c.startOrder(), position, c.totalScore(), c.scoreRating(), tied, c.status(), c.exercises()));
    }
}
