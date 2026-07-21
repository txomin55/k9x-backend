package com.k9x.application.events.obdx.use_case;

import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.use_case.dto.*;
import com.k9x.application.events.obdx.use_case.port.ClassificationCacheManagerPort;
import com.k9x.domain.disciplines.valueobjects.Discipline;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.disciplines.obdx.ObdxCacobAwards;
import com.k9x.domain.disciplines.obdx.ObdxCompetitorEventScore;
import com.k9x.domain.disciplines.obdx.ObdxQualification;
import com.k9x.domain.disciplines.obdx.ObdxRanking;
import com.k9x.domain.disciplines.obdx.ObdxScoreAveraging;
import com.k9x.domain.disciplines.obdx.ObdxScoreRating;
import com.k9x.domain.events.status.ClassificationCompetitorStatus;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.valueobjects.EventCompetitor;
import com.k9x.domain.events.valueobjects.EventExercise;
import com.k9x.domain.events.valueobjects.EventJudge;
import com.k9x.domain.events.valueobjects.Score;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class GetObdxClassificationServiceCase {

    private final GetObdxClassificationConfigPort getObdxClassificationConfigPort;
    private final ClassificationCacheManagerPort classificationCacheManagerPort;

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
        Map<String, EventJudge> judgesById = (event.judges() == null ? List.<EventJudge>of() : event.judges())
                .stream()
                .collect(Collectors.toMap(EventJudge::judgeId, j -> j, (a, _) -> a));
        List<Score> scores = event.scores() == null ? List.of() : event.scores();

        List<EventExercise> sortedExercises = new ArrayList<>(exercises);
        sortedExercises.sort(Comparator.comparing(
                e -> e.position() == null ? Short.MAX_VALUE : e.position()));

        List<FetchClassificationRawRowDTO> rows = new ArrayList<>();
        for (EventCompetitor c : competitors) {
            for (EventExercise ex : sortedExercises) {
                List<String> assignedJudgeIds = ex.judges() == null ? List.of() : ex.judges();
                for (String judgeId : assignedJudgeIds) {
                    EventJudge jd = judgesById.get(judgeId);
                    if (jd == null) {
                        continue;
                    }
                    Score s = scores.stream()
                            .filter(sc -> Objects.equals(sc.exerciseId(), ex.exerciseId())
                                    && Objects.equals(sc.judgeId(), jd.judgeId())
                                    && Objects.equals(sc.dogId(), c.dogId()))
                            .findFirst().orElse(null);
                    rows.add(new FetchClassificationRawRowDTO(
                            c.dogId(), c.dogName(), c.breed(), c.owner(), c.handler(), c.team(), c.country(),
                            ex.exerciseId(), ex.position() == null ? (short) 0 : ex.position(),
                            ex.tags() == null ? null : ex.tags().toArray(new String[0]),
                            jd.judgeId(), jd.judgeName(),
                            s == null ? null : s.score(),
                            s == null ? null : s.lastUpdate(),
                            s == null ? null : s.yellowCard(),
                            s == null ? null : s.redCard()));
                }
            }
        }
        return rows;
    }

    private FetchObdxClassificationDTO aggregateProjection(EventSnapshot event,
                                                           ObdxClassificationConfigDTO config,
                                                           List<FetchClassificationRawRowDTO> rawRows) {
        Map<String, Integer> judgeCountByExercise = (event.exercises() == null ? List.<EventExercise>of() : event.exercises())
                .stream()
                .collect(Collectors.toMap(EventExercise::exerciseId,
                        e -> e.judges() == null ? 0 : e.judges().size(), (a, _) -> a));
        // dogId → exerciseId → list of (judgeId, judgeName, score)
        Map<String, Map<String, List<FetchClassificationJudgeScoreDTO>>> judgeScoresByDogExercise = new LinkedHashMap<>();
        // dogId → exerciseId → list of yellow cards stamped for that exercise (one per stamped slot, across judges)
        Map<String, Map<String, List<FetchClassificationYellowCardDTO>>> yellowCardsByDogExercise = new LinkedHashMap<>();
        // dogId → exerciseId → the red card stamped there, if any (only one can ever exist per dog in the event)
        Map<String, Map<String, FetchClassificationRedCardDTO>> redCardByDogExercise = new LinkedHashMap<>();
        // dog metadata keyed by dogId (first row wins)
        Map<String, FetchClassificationRawRowDTO> dogMeta = new LinkedHashMap<>();
        // exerciseId → position and tags (same for all dogs)
        Map<String, Short> exercisePositions = new LinkedHashMap<>();
        Map<String, List<String>> exerciseTags = new LinkedHashMap<>();
        // dogId → static start order, set on enrollment
        Map<String, Short> startOrderByDog = new LinkedHashMap<>();
        // dogId → competitor number (dorsal), set on enrollment; independent from the start order
        Map<String, Short> competitorNumberByDog = new LinkedHashMap<>();
        // dogId → best in show flag, set on enrollment
        Map<String, Boolean> bihByDog = new LinkedHashMap<>();
        // dogId → reserve flag, set on enrollment
        Map<String, Boolean> reserveByDog = new LinkedHashMap<>();
        // dogId → not competing flag, set on enrollment
        Map<String, Boolean> notCompetingByDog = new LinkedHashMap<>();
        // dogId → whether the dog has 3 FCI generations confirmed, used to resolve CACOB/CACIOB awards
        Map<String, Boolean> fciConfirmedByDog = new LinkedHashMap<>();
        for (EventCompetitor competitor : (event.competitors() == null ? List.<EventCompetitor>of() : event.competitors())) {
            startOrderByDog.put(competitor.dogId(), competitor.startNumber());
            competitorNumberByDog.put(competitor.dogId(), competitor.competitorNumber());
            bihByDog.put(competitor.dogId(), competitor.bih());
            reserveByDog.put(competitor.dogId(), competitor.reserve());
            notCompetingByDog.put(competitor.dogId(), competitor.notCompeting());
            fciConfirmedByDog.put(competitor.dogId(), competitor.threeFciGenerationsConfirmed());
        }

        List<ObdxQualification.Tier> qualificationTiers = qualificationTiers(config);
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

            if (row.redCard() != null) {
                redCardByDogExercise
                        .computeIfAbsent(row.dogId(), _ -> new LinkedHashMap<>())
                        .put(row.exerciseId(), new FetchClassificationRedCardDTO(row.judgeId(), row.judgeName(), row.redCard()));
            }

            if (row.score() != null) {
                BigDecimal judgeScoreRating = ObdxScoreRating.percentageOfMax(row.score(), config.maxAllowedScore());
                List<FetchClassificationJudgeScoreDTO> exerciseScores = judgeScoresByDogExercise.get(row.dogId()).get(row.exerciseId());
                exerciseScores.add(new FetchClassificationJudgeScoreDTO(row.judgeId(), row.judgeName(), row.score(), judgeScoreRating, true));
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
            boolean anyExerciseScored = false;
            List<FetchClassificationExerciseScoreDTO> exercises = new ArrayList<>();

            for (Map.Entry<String, List<FetchClassificationJudgeScoreDTO>> exEntry : dogEntry.getValue().entrySet()) {
                String exerciseId = exEntry.getKey();
                List<FetchClassificationJudgeScoreDTO> judgeEntries =
                        withApplies(exEntry.getValue(), event.scoreCalculation());
                List<BigDecimal> scores = judgeEntries.stream().map(FetchClassificationJudgeScoreDTO::score).toList();

                BigDecimal coef = config.coefByExerciseId().getOrDefault(exerciseId, BigDecimal.ONE);
                List<FetchClassificationYellowCardDTO> exerciseYellowCards = yellowCardsByDogExercise
                        .getOrDefault(dogId, Map.of()).getOrDefault(exerciseId, List.of());
                FetchClassificationRedCardDTO exerciseRedCard = redCardByDogExercise
                        .getOrDefault(dogId, Map.of()).get(exerciseId);
                // maxExerciseScore is the maximum attainable for this exercise (highest allowed score * coef); it is a
                // constant reference. weightedScore is what the competitor has actually achieved: the judges' average
                // (or mid-avg) times the coef, which is null while the exercise has no scores, minus a flat penalty
                // if a yellow card was stamped for this exercise (never below zero).
                BigDecimal maxExerciseScore = ObdxScoreRating.maxExerciseScore(config.maxAllowedScore(), coef);
                BigDecimal weightedScore = scores.isEmpty() ? null
                        : ObdxScoreRating.weightedScore(
                        ObdxScoreAveraging.average(scores, event.scoreCalculation(),
                                judgeCountByExercise.getOrDefault(exerciseId, 0)),
                        coef);
                if (weightedScore != null && !exerciseYellowCards.isEmpty()) {
                    weightedScore = ObdxScoreRating.applyYellowCardPenalty(weightedScore);
                }
                BigDecimal exerciseScoreRating = weightedScore == null ? null
                        : ObdxScoreRating.percentageOfMax(weightedScore, maxExerciseScore);

                if (weightedScore != null) {
                    computedTotal = computedTotal.add(weightedScore);
                    anyExerciseScored = true;
                }
                exercises.add(new FetchClassificationExerciseScoreDTO(
                        exerciseId,
                        exercisePositions.getOrDefault(exerciseId, (short) 0),
                        exerciseTags.getOrDefault(exerciseId, List.of()),
                        maxExerciseScore, weightedScore, exerciseScoreRating,
                        judgeEntries, exerciseYellowCards, exerciseRedCard));
            }

            BigDecimal totalScore = computedTotal;

            BigDecimal maxPossibleTotal = ObdxScoreRating.maxPossibleTotal(
                    config.maxAllowedScore(), config.coefByExerciseId(), exercisePositions.keySet());
            BigDecimal competitorScoreRating = ObdxScoreRating.percentageOfMax(totalScore, maxPossibleTotal);

            ClassificationCompetitorStatus status;
            if (event.isCompetitorSettled(dogId)) {
                status = ClassificationCompetitorStatus.SETTLED;
            } else if (event.isCompetitorStarted(dogId)) {
                status = ClassificationCompetitorStatus.LIVE;
            } else {
                status = ClassificationCompetitorStatus.PENDING;
            }

            boolean disqualifiedOrNotCompeting = event.isDisqualified(dogId) || event.isNotCompeting(dogId);
            boolean hasScore = anyExerciseScored;
            String qualification = ObdxQualification.resolve(qualificationTiers, totalScore, disqualifiedOrNotCompeting, hasScore);
            BigDecimal rankScore = ObdxCompetitorEventScore.ofEvent(
                    event.rankScore(), event.configurationId(),
                    ObdxQualification.minThreshold(qualificationTiers),
                    ObdxQualification.maxThreshold(qualificationTiers),
                    totalScore, maxPossibleTotal, hasScore);

            competitors.add(new FetchClassificationCompetitorDTO(
                    dogId, meta.dogName(), meta.dogBreed(), meta.dogOwner(), meta.dogHandler(), meta.dogTeam(), meta.dogCountry(),
                    startOrderByDog.get(dogId), competitorNumberByDog.get(dogId), 0, totalScore, competitorScoreRating, false,
                    status.name(), bihByDog.get(dogId), reserveByDog.get(dogId),
                    Boolean.TRUE.equals(notCompetingByDog.get(dogId)), exercises,
                    List.of(), qualification, rankScore));
        }

        assignPositions(competitors, config);
        assignCacobAwards(competitors, event.awards(), fciConfirmedByDog);

        List<FetchObdxEventJudgeDTO> judges = (event.judges() == null ? List.<EventJudge>of() : event.judges())
                .stream()
                .map(j -> new FetchObdxEventJudgeDTO(j.judgeId(), j.judgeName(), j.collectorEmail()))
                .toList();

        return new FetchObdxClassificationDTO(scoresLastUpdate, competitors,
                event.scoreCalculation() == null ? null : event.scoreCalculation().name(), judges);
    }

    /** Maps the federation config's qualification thresholds to the domain qualification tiers. */
    private List<ObdxQualification.Tier> qualificationTiers(ObdxClassificationConfigDTO config) {
        if (config.qualifications() == null) {
            return List.of();
        }
        return config.qualifications().stream()
                .map(t -> new ObdxQualification.Tier(t.id(), t.minScore()))
                .toList();
    }

    /** Builds the CACOB/CACIOB candidates in ranking order and applies the awards the domain policy assigns. */
    private void assignCacobAwards(List<FetchClassificationCompetitorDTO> competitors, List<String> eventAwards,
                                   Map<String, Boolean> fciConfirmedByDog) {
        List<ObdxCacobAwards.Candidate> candidates = competitors.stream()
                .map(c -> new ObdxCacobAwards.Candidate(
                        Boolean.TRUE.equals(fciConfirmedByDog.get(c.dogId())), c.scoreRating(), c.position()))
                .toList();
        ObdxCacobAwards.assign(candidates, eventAwards)
                .forEach((index, awards) -> awards.forEach(award -> addAward(competitors, index, award)));
    }

    private void addAward(List<FetchClassificationCompetitorDTO> competitors, int index, String award) {
        FetchClassificationCompetitorDTO c = competitors.get(index);
        List<String> awards = new ArrayList<>(c.awards());
        awards.add(award);
        competitors.set(index, new FetchClassificationCompetitorDTO(
                c.dogId(), c.dogName(), c.breed(), c.owner(), c.handler(), c.team(), c.country(),
                c.startOrder(), c.competitorNumber(), c.position(), c.totalScore(), c.scoreRating(), c.tied(), c.status(), c.bih(),
                c.reserve(), c.notCompeting(), c.exercises(), awards, c.qualification(), c.rankScore()));
    }

    /**
     * Flags each judge score with whether it actually applied to the exercise average: under MID_AVG the single
     * highest and single lowest score are dropped (see {@link ObdxScoreAveraging#excludedIndexes}); everything
     * else — including every score under AVG — applies.
     */
    private List<FetchClassificationJudgeScoreDTO> withApplies(List<FetchClassificationJudgeScoreDTO> judgeEntries,
                                                                ObdxAvgMethod method) {
        Set<Integer> excluded = ObdxScoreAveraging.excludedIndexes(
                judgeEntries.stream().map(FetchClassificationJudgeScoreDTO::score).toList(), method);
        if (excluded.isEmpty()) {
            return judgeEntries;
        }
        List<FetchClassificationJudgeScoreDTO> result = new ArrayList<>();
        for (int i = 0; i < judgeEntries.size(); i++) {
            FetchClassificationJudgeScoreDTO j = judgeEntries.get(i);
            result.add(new FetchClassificationJudgeScoreDTO(j.judgeId(), j.judgeName(), j.score(), j.scoreRating(),
                    !excluded.contains(i)));
        }
        return result;
    }

    private void assignPositions(List<FetchClassificationCompetitorDTO> competitors,
                                 ObdxClassificationConfigDTO config) {
        Comparator<FetchClassificationCompetitorDTO> comparator =
                Comparator.comparing(this::rankingTier)
                        .thenComparing(Comparator.comparing(FetchClassificationCompetitorDTO::totalScore).reversed())
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

    private int rankingTier(FetchClassificationCompetitorDTO competitor) {
        return ObdxRanking.tier(competitor.notCompeting(), hasRedCard(competitor));
    }

    private boolean hasRedCard(FetchClassificationCompetitorDTO competitor) {
        return competitor.exercises().stream().anyMatch(e -> e.redCard() != null);
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
        return rankingTier(a) == rankingTier(b)
                && a.totalScore().compareTo(b.totalScore()) == 0
                && tieScore(a, config.breakTie()).compareTo(tieScore(b, config.breakTie())) == 0
                && tieScore(a, config.breakTieTie()).compareTo(tieScore(b, config.breakTieTie())) == 0;
    }

    private void setPosition(List<FetchClassificationCompetitorDTO> competitors, int index, int position, boolean tied) {
        FetchClassificationCompetitorDTO c = competitors.get(index);
        competitors.set(index, new FetchClassificationCompetitorDTO(
                c.dogId(), c.dogName(), c.breed(), c.owner(), c.handler(), c.team(), c.country(),
                c.startOrder(), c.competitorNumber(), position, c.totalScore(), c.scoreRating(), tied, c.status(), c.bih(),
                c.reserve(), c.notCompeting(), c.exercises(), c.awards(), c.qualification(), c.rankScore()));
    }
}
