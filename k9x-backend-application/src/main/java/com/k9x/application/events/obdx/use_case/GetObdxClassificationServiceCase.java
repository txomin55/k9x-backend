package com.k9x.application.events.obdx.use_case;

import com.k9x.application.events.obdx.exceptions.ObdxNotEnoughJudgesException;
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
import java.util.stream.Collectors;

public class GetObdxClassificationServiceCase {

    private final GetObdxClassificationConfigPort getObdxClassificationConfigPort;
    private final ClassificationCacheManagerPort classificationCacheManagerPort;

    private static final BigDecimal YELLOW_CARD_PENALTY = BigDecimal.TEN;
    private static final BigDecimal CACOB_MIN_SCORE_RATING = new BigDecimal("80");
    /** Fallback qualification when the total score does not reach the lowest configured tier. */
    private static final String NOT_CLASSIFIED_QUALIFICATION = "NC";
    /** Qualification for a competitor that is disqualified (red card / second yellow) or not competing. */
    private static final String DISQUALIFIED_QUALIFICATION = "DISQ";

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
        // dogId → static start order (competitor number), set on enrollment
        Map<String, Short> startOrderByDog = new LinkedHashMap<>();
        // dogId → best in show flag, set on enrollment
        Map<String, Boolean> bihByDog = new LinkedHashMap<>();
        // dogId → reserve flag, set on enrollment
        Map<String, Boolean> reserveByDog = new LinkedHashMap<>();
        // dogId → not competing flag, set on enrollment
        Map<String, Boolean> notCompetingByDog = new LinkedHashMap<>();
        // dogId → whether the dog has 3 FCI generations confirmed, used to resolve CACOB/CACIOB awards
        Map<String, Boolean> fciConfirmedByDog = new LinkedHashMap<>();
        for (EventCompetitor competitor : (event.competitors() == null ? List.<EventCompetitor>of() : event.competitors())) {
            startOrderByDog.put(competitor.dogId(), competitor.position());
            bihByDog.put(competitor.dogId(), competitor.bih());
            reserveByDog.put(competitor.dogId(), competitor.reserve());
            notCompetingByDog.put(competitor.dogId(), competitor.notCompeting());
            fciConfirmedByDog.put(competitor.dogId(), competitor.threeFciGenerationsConfirmed());
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

            if (row.redCard() != null) {
                redCardByDogExercise
                        .computeIfAbsent(row.dogId(), _ -> new LinkedHashMap<>())
                        .put(row.exerciseId(), new FetchClassificationRedCardDTO(row.judgeId(), row.judgeName(), row.redCard()));
            }

            if (row.score() != null) {
                BigDecimal judgeScoreRating = percentageOfMax(row.score(), config.maxAllowedScore());
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
                // exerciseScore is the maximum attainable for this exercise (highest allowed score * coef); it is a
                // constant reference. totalScore is what the competitor has actually achieved: the judges' average
                // (or mid-avg) times the coef, which is 0 while the exercise has no scores, minus a flat penalty
                // if a yellow card was stamped for this exercise (never below zero).
                BigDecimal maxExerciseScore = config.maxAllowedScore().multiply(coef).setScale(2, RoundingMode.HALF_UP);
                BigDecimal weightedScore = scores.isEmpty() ? null
                        : computeAvg(scores, event.scoreCalculation(), judgeCountByExercise.getOrDefault(exerciseId, 0))
                        .multiply(coef).setScale(2, RoundingMode.HALF_UP);
                if (weightedScore != null && !exerciseYellowCards.isEmpty()) {
                    weightedScore = weightedScore.subtract(YELLOW_CARD_PENALTY).max(BigDecimal.ZERO);
                }
                BigDecimal exerciseScoreRating = weightedScore == null ? null
                        : percentageOfMax(weightedScore, maxExerciseScore);

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

            boolean disqualifiedOrNotCompeting = event.isDisqualified(dogId) || event.isNotCompeting(dogId);
            boolean hasScore = anyExerciseScored;
            String qualification = resolveQualification(config, totalScore, disqualifiedOrNotCompeting, hasScore);

            competitors.add(new FetchClassificationCompetitorDTO(
                    dogId, meta.dogName(), meta.dogBreed(), meta.dogOwner(), meta.dogHandler(), meta.dogTeam(), meta.dogCountry(),
                    startOrderByDog.get(dogId), 0, totalScore, competitorScoreRating, false,
                    status.name(), bihByDog.get(dogId), reserveByDog.get(dogId),
                    Boolean.TRUE.equals(notCompetingByDog.get(dogId)), exercises,
                    List.of(), qualification));
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

    /**
     * Awards CACOB/CACIOB (and their reserve RCACOB/RCACIOB) when the event enables them. A competitor "qualifies"
     * when its dog has {@code threeFciGenerationsConfirmed} and its score rating is above
     * {@link #CACOB_MIN_SCORE_RATING}. The main award only goes to the overall winner ({@code position() == 1})
     * if it qualifies — no substitute winner is promoted when it doesn't. The reserve award goes to the next
     * qualifying competitor found walking down the ranking, skipping the main award's recipient if there was one;
     * when the winner didn't qualify (so no main award was granted to anyone), the reserve simply goes to the
     * first qualifying competitor in the ranking, whatever their position.
     */
    private void assignCacobAwards(List<FetchClassificationCompetitorDTO> competitors, List<String> eventAwards,
                                   Map<String, Boolean> fciConfirmedByDog) {
        if (eventAwards == null) {
            return;
        }
        if (eventAwards.contains("CACOB")) {
            assignCacobAward(competitors, fciConfirmedByDog, "CACOB", "RCACOB");
        }
        if (eventAwards.contains("CACIOB")) {
            assignCacobAward(competitors, fciConfirmedByDog, "CACIOB", "RCACIOB");
        }
    }

    private void assignCacobAward(List<FetchClassificationCompetitorDTO> competitors,
                                  Map<String, Boolean> fciConfirmedByDog, String mainAward, String reserveAward) {
        List<Integer> qualifyingIndexes = new ArrayList<>();
        for (int i = 0; i < competitors.size(); i++) {
            FetchClassificationCompetitorDTO competitor = competitors.get(i);
            if (Boolean.TRUE.equals(fciConfirmedByDog.get(competitor.dogId()))
                    && competitor.scoreRating() != null
                    && competitor.scoreRating().compareTo(CACOB_MIN_SCORE_RATING) > 0) {
                qualifyingIndexes.add(i);
            }
        }
        if (qualifyingIndexes.isEmpty()) {
            return;
        }

        int reserveCandidate = 0;
        int firstIndex = qualifyingIndexes.get(0);
        if (competitors.get(firstIndex).position() == 1) {
            addAward(competitors, firstIndex, mainAward);
            reserveCandidate = 1;
        }
        if (reserveCandidate < qualifyingIndexes.size()) {
            addAward(competitors, qualifyingIndexes.get(reserveCandidate), reserveAward);
        }
    }

    private void addAward(List<FetchClassificationCompetitorDTO> competitors, int index, String award) {
        FetchClassificationCompetitorDTO c = competitors.get(index);
        List<String> awards = new ArrayList<>(c.awards());
        awards.add(award);
        competitors.set(index, new FetchClassificationCompetitorDTO(
                c.dogId(), c.dogName(), c.breed(), c.owner(), c.handler(), c.team(), c.country(),
                c.startOrder(), c.position(), c.totalScore(), c.scoreRating(), c.tied(), c.status(), c.bih(),
                c.reserve(), c.notCompeting(), c.exercises(), awards, c.qualification()));
    }

    /**
     * Text qualification (calificativo) for the competitor, resolved in this order, independently of the numeric
     * total score for the first two cases:
     * <ol>
     *     <li>a disqualified (red card / second yellow) or not-competing competitor is
     *     {@link #DISQUALIFIED_QUALIFICATION} (DISQ);</li>
     *     <li>a competitor with no recorded score has no qualification yet ({@code null});</li>
     *     <li>otherwise it is the id of the highest configured tier whose {@code minScore} the total score reaches,
     *     or {@link #NOT_CLASSIFIED_QUALIFICATION} (NC) when it reaches none.</li>
     * </ol>
     * Returns {@code null} when the federation configuration defines no qualification scale, so the field stays
     * absent for grades that don't use it.
     */
    private String resolveQualification(ObdxClassificationConfigDTO config, BigDecimal totalScore,
                                        boolean disqualifiedOrNotCompeting, boolean hasScore) {
        List<ObdxClassificationConfigDTO.QualificationThreshold> tiers = config.qualifications();
        if (tiers == null || tiers.isEmpty()) {
            return null;
        }
        if (disqualifiedOrNotCompeting) {
            return DISQUALIFIED_QUALIFICATION;
        }
        if (!hasScore) {
            return null;
        }
        return tiers.stream()
                .filter(t -> t.minScore() != null && totalScore.compareTo(t.minScore()) >= 0)
                .max(Comparator.comparing(ObdxClassificationConfigDTO.QualificationThreshold::minScore))
                .map(ObdxClassificationConfigDTO.QualificationThreshold::id)
                .orElse(NOT_CLASSIFIED_QUALIFICATION);
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

    private BigDecimal computeAvg(List<BigDecimal> scores, ObdxAvgMethod method, int judgeCount) {
        if (scores.isEmpty()) return BigDecimal.ZERO;
        if (method == ObdxAvgMethod.MID_AVG) {
            if (judgeCount < 4) throw new ObdxNotEnoughJudgesException();
            if (scores.size() >= 4) {
                List<BigDecimal> trimmed = new ArrayList<>(scores);
                trimmed.remove(Collections.min(trimmed));
                trimmed.remove(Collections.max(trimmed));
                return average(trimmed);
            }
        }
        return average(scores);
    }

    private BigDecimal average(List<BigDecimal> values) {
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
    }

    /**
     * Flags each judge score with whether it actually applied to {@link #computeAvg}: under MID_AVG the single
     * highest and single lowest score are dropped regardless of how many judges have scored so far (a tie at
     * an extreme only excludes one occurrence, matching {@link #computeAvg}'s removal once it does trim at 4+
     * scores); everything else — including every score under AVG — applies.
     */
    private List<FetchClassificationJudgeScoreDTO> withApplies(List<FetchClassificationJudgeScoreDTO> judgeEntries,
                                                                ObdxAvgMethod method) {
        Set<Integer> excluded = excludedJudgeIndexes(
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

    private Set<Integer> excludedJudgeIndexes(List<BigDecimal> scores, ObdxAvgMethod method) {
        if (method != ObdxAvgMethod.MID_AVG || scores.isEmpty()) {
            return Set.of();
        }
        List<BigDecimal> working = new ArrayList<>(scores);
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++) {
            indices.add(i);
        }

        int minPos = working.indexOf(Collections.min(working));
        int minOriginalIndex = indices.remove(minPos);
        working.remove(minPos);

        if (working.isEmpty()) {
            return Set.of(minOriginalIndex);
        }

        int maxPos = working.indexOf(Collections.max(working));
        int maxOriginalIndex = indices.remove(maxPos);

        return new HashSet<>(List.of(minOriginalIndex, maxOriginalIndex));
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

    /**
     * Ranking tier used as the primary sort key: regular competitors first, then red-carded (disqualified)
     * competitors ordered by score among themselves, then not-competing competitors last.
     */
    private int rankingTier(FetchClassificationCompetitorDTO competitor) {
        if (competitor.notCompeting()) {
            return 2;
        }
        if (hasRedCard(competitor)) {
            return 1;
        }
        return 0;
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
                c.startOrder(), position, c.totalScore(), c.scoreRating(), tied, c.status(), c.bih(),
                c.reserve(), c.notCompeting(), c.exercises(), c.awards(), c.qualification()));
    }
}
