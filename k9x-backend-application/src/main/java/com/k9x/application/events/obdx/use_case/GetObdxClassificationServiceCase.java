package com.k9x.application.events.obdx.use_case;

import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.use_case.dto.*;
import com.k9x.application.events.obdx.use_case.port.ClassificationCacheManagerPort;
import com.k9x.domain.disciplines.valueobjects.Discipline;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.disciplines.obdx.ObdxCacobAwards;
import com.k9x.domain.disciplines.obdx.ObdxCompetitorEventScore;
import com.k9x.domain.disciplines.obdx.ObdxFinalScoreExercise;
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
            // The static final score is the competitor's whole total, so it supersedes any per-exercise score it
            // might also hold: emit no exercise rows for such a competitor at all.
            if (event.finalScore(c.dogIdentification()) != null) {
                continue;
            }
            for (EventExercise ex : sortedExercises) {
                List<String> assignedJudgeIds = ex.judges() == null ? List.of() : ex.judges();
                for (String judgeId : assignedJudgeIds) {
                    EventJudge jd = resolveAssignedJudge(judgesById, judgeId);
                    if (jd == null) {
                        continue;
                    }
                    Score s = scores.stream()
                            .filter(sc -> Objects.equals(sc.exerciseId(), ex.exerciseId())
                                    && Objects.equals(sc.judgeId(), jd.judgeId())
                                    && Objects.equals(sc.dogIdentification(), c.dogIdentification()))
                            .findFirst().orElse(null);
                    rows.add(new FetchClassificationRawRowDTO(
                            c.dogIdentification(), c.dogName(), c.breed(), c.owner(), c.handler(), c.team(), c.country(),
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

    /**
     * The judge an exercise assignment resolves to, or {@code null} when the assignment names nobody the event
     * knows and the row has to be dropped.
     *
     * <p>Normally the assignment is one of the event's own judges. The <strong>anonymous</strong> judges —
     * {@code UNKNOWN} and the numbered {@code UNKNOWN_1}, {@code UNKNOWN_2}…, see
     * {@link ObdxFinalScoreExercise#isUnknownJudge} — are the exception, and they are resolved here instead of
     * being looked up in the panel: none of them is a person, so none has any business in
     * {@code obdx.event_judges}. That table is the jury of the trial, and an event listing UNKNOWN there would
     * render it as one of its judges.
     *
     * <p>The case is an extraction that knows <em>what</em> each exercise scored but not <em>who</em> gave the
     * mark. Those scores go to the anonymous slots — {@code obdx.event_scores.judge_id} is {@code NOT NULL} and
     * part of the primary key, so there is nowhere else to put them — while the event keeps declaring its real
     * panel. Without this the two id sets never intersected and <em>every</em> competitor of such an event
     * vanished from the classification, silently and with a 200: the scores are only ever reached through this
     * assignment, so the lookup below never ran. The averaging denominator stays right on its own, because it
     * counts the exercise's assigned judges — as many as there are marks — and not the panel.
     */
    private EventJudge resolveAssignedJudge(Map<String, EventJudge> judgesById, String judgeId) {
        EventJudge judge = judgesById.get(judgeId);
        if (judge != null) {
            return judge;
        }
        return ObdxFinalScoreExercise.isUnknownJudge(judgeId)
                ? new EventJudge(judgeId, judgeId, null, false)
                : null;
    }

    private FetchObdxClassificationDTO aggregateProjection(EventSnapshot event,
                                                           ObdxClassificationConfigDTO config,
                                                           List<FetchClassificationRawRowDTO> rawRows) {
        // The panel of the EVENT, which is what MID_AVG's minimum is about. Not the judges assigned to each
        // exercise: a four-judge trial that splits them across rings has exercises scored by two of them, and
        // that is still a four-judge trial. The trim itself keys off how many actually scored.
        int panelSize = event.judges() == null ? 0 : event.judges().size();
        // dogIdentification → exerciseId → list of (judgeId, judgeName, score)
        Map<String, Map<String, List<FetchClassificationJudgeScoreDTO>>> judgeScoresByDogExercise = new LinkedHashMap<>();
        // dogIdentification → exerciseId → list of yellow cards stamped for that exercise (one per stamped slot, across judges)
        Map<String, Map<String, List<FetchClassificationYellowCardDTO>>> yellowCardsByDogExercise = new LinkedHashMap<>();
        // dogIdentification → exerciseId → the red card stamped there, if any (only one can ever exist per dog in the event)
        Map<String, Map<String, FetchClassificationRedCardDTO>> redCardByDogExercise = new LinkedHashMap<>();
        // dog metadata keyed by dogIdentification (first row wins)
        Map<String, FetchClassificationRawRowDTO> dogMeta = new LinkedHashMap<>();
        // exerciseId → position and tags (same for all dogs)
        Map<String, Short> exercisePositions = new LinkedHashMap<>();
        Map<String, List<String>> exerciseTags = new LinkedHashMap<>();
        // dogIdentification → static start order, set on enrollment
        Map<String, Short> startOrderByDog = new LinkedHashMap<>();
        // dogIdentification → competitor number (dorsal), set on enrollment; independent from the start order
        Map<String, Short> competitorNumberByDog = new LinkedHashMap<>();
        // dogIdentification → best in show flag, set on enrollment
        Map<String, Boolean> bihByDog = new LinkedHashMap<>();
        // dogIdentification → reserve flag, set on enrollment
        Map<String, Boolean> reserveByDog = new LinkedHashMap<>();
        // dogIdentification → not competing flag, set on enrollment
        Map<String, Boolean> notCompetingByDog = new LinkedHashMap<>();
        // dogIdentification → whether the dog has 3 FCI generations confirmed, used to resolve CACOB/CACIOB awards
        Map<String, Boolean> fciConfirmedByDog = new LinkedHashMap<>();
        for (EventCompetitor competitor : (event.competitors() == null ? List.<EventCompetitor>of() : event.competitors())) {
            startOrderByDog.put(competitor.dogIdentification(), competitor.startNumber());
            competitorNumberByDog.put(competitor.dogIdentification(), competitor.competitorNumber());
            bihByDog.put(competitor.dogIdentification(), competitor.bih());
            reserveByDog.put(competitor.dogIdentification(), competitor.reserve());
            notCompetingByDog.put(competitor.dogIdentification(), competitor.notCompeting());
            fciConfirmedByDog.put(competitor.dogIdentification(), competitor.threeFciGenerationsConfirmed());
        }

        List<ObdxQualification.Tier> qualificationTiers = qualificationTiers(config);
        Long scoresLastUpdate = null;

        // Competitors carrying a static final score produce no raw rows, so seed their entries here: an empty
        // exercise map (nothing to break down) plus the metadata the projection reads off the first raw row.
        Map<String, BigDecimal> finalScoreByDog = new LinkedHashMap<>();
        for (EventCompetitor competitor : (event.competitors() == null ? List.<EventCompetitor>of() : event.competitors())) {
            Score staticScore = event.finalScore(competitor.dogIdentification());
            if (staticScore == null) {
                continue;
            }
            finalScoreByDog.put(competitor.dogIdentification(), staticScore.score());
            scoresLastUpdate = scoresLastUpdate == null
                    ? staticScore.lastUpdate() : Math.max(scoresLastUpdate, staticScore.lastUpdate());
            judgeScoresByDogExercise.put(competitor.dogIdentification(), new LinkedHashMap<>());
            dogMeta.put(competitor.dogIdentification(), new FetchClassificationRawRowDTO(
                    competitor.dogIdentification(), competitor.dogName(), competitor.breed(), competitor.owner(),
                    competitor.handler(), competitor.team(), competitor.country(),
                    null, (short) 0, null, null, null, null, null, null, null));
        }

        for (FetchClassificationRawRowDTO row : rawRows) {
            dogMeta.putIfAbsent(row.dogIdentification(), row);
            exercisePositions.putIfAbsent(row.exerciseId(), row.exercisePosition());
            exerciseTags.putIfAbsent(row.exerciseId(),
                    row.exerciseTags() != null ? Arrays.asList(row.exerciseTags()) : List.of());

            judgeScoresByDogExercise
                    .computeIfAbsent(row.dogIdentification(), _ -> new LinkedHashMap<>())
                    .computeIfAbsent(row.exerciseId(), _ -> new ArrayList<>());
            List<FetchClassificationYellowCardDTO> exerciseYellowCards = yellowCardsByDogExercise
                    .computeIfAbsent(row.dogIdentification(), _ -> new LinkedHashMap<>())
                    .computeIfAbsent(row.exerciseId(), _ -> new ArrayList<>());

            if (row.yellowCard() != null) {
                exerciseYellowCards.add(
                        new FetchClassificationYellowCardDTO(row.judgeId(), row.judgeName(), row.yellowCard()));
            }

            if (row.redCard() != null) {
                redCardByDogExercise
                        .computeIfAbsent(row.dogIdentification(), _ -> new LinkedHashMap<>())
                        .put(row.exerciseId(), new FetchClassificationRedCardDTO(row.judgeId(), row.judgeName(), row.redCard()));
            }

            if (row.score() != null) {
                BigDecimal judgeScoreRating = ObdxScoreRating.percentageOfMax(row.score(), config.maxAllowedScore());
                List<FetchClassificationJudgeScoreDTO> exerciseScores = judgeScoresByDogExercise.get(row.dogIdentification()).get(row.exerciseId());
                exerciseScores.add(new FetchClassificationJudgeScoreDTO(row.judgeId(), row.judgeName(), row.score(), judgeScoreRating, true));
                scoresLastUpdate = scoresLastUpdate == null
                        ? row.scoreLastUpdate() : Math.max(scoresLastUpdate, row.scoreLastUpdate());
            }
        }

        List<FetchClassificationCompetitorDTO> competitors = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<FetchClassificationJudgeScoreDTO>>> dogEntry
                : judgeScoresByDogExercise.entrySet()) {
            String dogIdentification = dogEntry.getKey();
            FetchClassificationRawRowDTO meta = dogMeta.get(dogIdentification);
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
                        .getOrDefault(dogIdentification, Map.of()).getOrDefault(exerciseId, List.of());
                FetchClassificationRedCardDTO exerciseRedCard = redCardByDogExercise
                        .getOrDefault(dogIdentification, Map.of()).get(exerciseId);
                // maxExerciseScore is the maximum attainable for this exercise (highest allowed score * coef); it is a
                // constant reference. weightedScore is what the competitor has actually achieved: the judges' average
                // (or mid-avg) times the coef, which is null while the exercise has no scores, minus a flat penalty
                // if a yellow card was stamped for this exercise (never below zero).
                BigDecimal maxExerciseScore = ObdxScoreRating.maxExerciseScore(config.maxAllowedScore(), coef);
                BigDecimal weightedScore = scores.isEmpty() ? null
                        : ObdxScoreRating.weightedScore(
                        ObdxScoreAveraging.average(scores, event.scoreCalculation(), panelSize),
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

            // A static final score replaces the computed total outright, and it is measured against the maximum of
            // every exercise the configuration defines: the event carries no exercise rows of its own to sum.
            BigDecimal finalScore = finalScoreByDog.get(dogIdentification);
            BigDecimal totalScore = finalScore != null ? finalScore : computedTotal;

            BigDecimal maxPossibleTotal = ObdxScoreRating.maxPossibleTotal(
                    config.maxAllowedScore(), config.coefByExerciseId(),
                    finalScore != null ? config.coefByExerciseId().keySet() : exercisePositions.keySet());
            BigDecimal competitorScoreRating = ObdxScoreRating.percentageOfMax(totalScore, maxPossibleTotal);

            ClassificationCompetitorStatus status;
            if (event.isCompetitorSettled(dogIdentification)) {
                status = ClassificationCompetitorStatus.SETTLED;
            } else if (event.isCompetitorStarted(dogIdentification)) {
                status = ClassificationCompetitorStatus.LIVE;
            } else {
                status = ClassificationCompetitorStatus.PENDING;
            }

            boolean disqualifiedOrNotCompeting = event.isDisqualified(dogIdentification) || event.isNotCompeting(dogIdentification);
            boolean hasScore = anyExerciseScored || finalScore != null;
            String qualification = ObdxQualification.resolve(qualificationTiers, totalScore, disqualifiedOrNotCompeting, hasScore);
            BigDecimal rankScore = ObdxCompetitorEventScore.ofEvent(
                    event.rankScore(), event.configurationId(),
                    ObdxQualification.minThreshold(qualificationTiers),
                    ObdxQualification.maxThreshold(qualificationTiers),
                    totalScore, maxPossibleTotal, hasScore, disqualifiedOrNotCompeting);

            competitors.add(new FetchClassificationCompetitorDTO(
                    dogIdentification, meta.dogName(), meta.dogBreed(), meta.dogOwner(), meta.dogHandler(), meta.dogTeam(), meta.dogCountry(),
                    startOrderByDog.get(dogIdentification), competitorNumberByDog.get(dogIdentification), 0, totalScore, competitorScoreRating, false,
                    status.name(), bihByDog.get(dogIdentification), reserveByDog.get(dogIdentification),
                    Boolean.TRUE.equals(notCompetingByDog.get(dogIdentification)), exercises,
                    List.of(), qualification, rankScore));
        }

        assignPositions(competitors, config);
        assignCacobAwards(competitors, event.awards(), fciConfirmedByDog);

        List<FetchObdxEventJudgeDTO> judges = (event.judges() == null ? List.<EventJudge>of() : event.judges())
                .stream()
                .map(j -> new FetchObdxEventJudgeDTO(j.judgeId(), j.judgeName(), j.collectorEmail(), j.mainJudge()))
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
                        Boolean.TRUE.equals(fciConfirmedByDog.get(c.dogIdentification())), c.scoreRating(), c.position()))
                .toList();
        ObdxCacobAwards.assign(candidates, eventAwards)
                .forEach((index, awards) -> awards.forEach(award -> addAward(competitors, index, award)));
    }

    private void addAward(List<FetchClassificationCompetitorDTO> competitors, int index, String award) {
        FetchClassificationCompetitorDTO c = competitors.get(index);
        List<String> awards = new ArrayList<>(c.awards());
        awards.add(award);
        competitors.set(index, new FetchClassificationCompetitorDTO(
                c.dogIdentification(), c.dogName(), c.breed(), c.owner(), c.handler(), c.team(), c.country(),
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
                c.dogIdentification(), c.dogName(), c.breed(), c.owner(), c.handler(), c.team(), c.country(),
                c.startOrder(), c.competitorNumber(), position, c.totalScore(), c.scoreRating(), tied, c.status(), c.bih(),
                c.reserve(), c.notCompeting(), c.exercises(), c.awards(), c.qualification(), c.rankScore()));
    }
}
