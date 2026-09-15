package com.k9x.infrastructure.in.rest.restricted;

import com.k9x.application.events.obdx.use_case.dto.FetchClassificationCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationExerciseScoreDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationJudgeScoreDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;

import java.util.List;

/**
 * Empties the scores of a classification whose extraction is restricted: the source published them but does not
 * allow them to be republished, so k9x keeps the data and stops showing the numbers.
 *
 * <p>It lives at the REST boundary and is applied by whoever asks for a classification — the public endpoint, the
 * xlsx export and the booklet proof — and deliberately NOT inside the use case: the aggregation, the persisted
 * snapshot, the classification cache, the ranking totals and the dog index are all computed from the real scores
 * and must keep being computed from them. Withholding earlier would poison every one of those.
 *
 * <p>What is emptied is what the competitor achieved — totals, per-exercise and per-judge points, their ratings
 * and the qualification derived from them. What stays is everything that is not a result: who ran, in which
 * order, where they placed, the exercises and their maximum, the judges and the cards.
 */
public final class WithheldScores {

    private WithheldScores() {
    }

    /** True when this classification's competition may not be republished. */
    public static boolean restricted(FetchClassificationDTO classification) {
        return classification != null && classification.extraction() != null
                && classification.extraction().restricted();
    }

    /** The same classification with every score emptied, or the very same instance when it is not restricted. */
    public static FetchClassificationDTO apply(FetchClassificationDTO classification) {
        if (!restricted(classification) || classification.obdx() == null) {
            return classification;
        }
        return new FetchClassificationDTO(
                classification.eventId(), classification.eventName(), classification.eventStatus(),
                classification.stageId(), classification.stageName(),
                classification.competitionName(),
                classification.disciplineId(),
                classification.configurationId(), classification.configurationName(),
                classification.scoresLastUpdate(),
                withheld(classification.obdx()),
                classification.rank(),
                classification.extraction());
    }

    private static FetchObdxClassificationDTO withheld(FetchObdxClassificationDTO obdx) {
        return new FetchObdxClassificationDTO(
                obdx.scoresLastUpdate(),
                obdx.competitors() == null ? null : obdx.competitors().stream()
                        .map(WithheldScores::withheld)
                        .toList(),
                obdx.scoreCalculation(),
                obdx.judges());
    }

    private static FetchClassificationCompetitorDTO withheld(FetchClassificationCompetitorDTO competitor) {
        return new FetchClassificationCompetitorDTO(
                competitor.dogIdentification(), competitor.dogName(), competitor.breed(), competitor.owner(),
                competitor.handler(), competitor.team(), competitor.country(),
                competitor.startOrder(), competitor.competitorNumber(), competitor.position(),
                null, null,
                competitor.tied(), competitor.status(),
                competitor.bih(), competitor.reserve(), competitor.notCompeting(),
                competitor.exercises() == null ? null : competitor.exercises().stream()
                        .map(WithheldScores::withheld)
                        .toList(),
                competitor.awards(), null, null);
    }

    /**
     * {@code exerciseScore} is the maximum attainable for the exercise — max allowed score times coefficient —
     * and not something the competitor did, so it stays: it is the scale the empty cell is read against.
     */
    private static FetchClassificationExerciseScoreDTO withheld(FetchClassificationExerciseScoreDTO exercise) {
        return new FetchClassificationExerciseScoreDTO(
                exercise.exerciseId(), exercise.exercisePosition(), exercise.tags(),
                exercise.exerciseScore(), null, null,
                exercise.judgeScores() == null ? List.of() : exercise.judgeScores().stream()
                        .map(WithheldScores::withheld)
                        .toList(),
                exercise.yellowCards(), exercise.redCard());
    }

    private static FetchClassificationJudgeScoreDTO withheld(FetchClassificationJudgeScoreDTO judgeScore) {
        return new FetchClassificationJudgeScoreDTO(judgeScore.judgeId(), judgeScore.judgeName(), null, null,
                judgeScore.applies());
    }
}
