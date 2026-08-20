package com.k9x.domain.disciplines.obdx;

import java.util.regex.Pattern;

/**
 * The grade-agnostic exercise that carries a competitor's <em>whole</em> event total instead of one judge's
 * mark for one exercise.
 *
 * <p>Imported events have no per-judge granularity: the source only publishes the final score each competitor
 * earned. Such an event therefore has no {@code obdx.event_exercises} rows at all — a single
 * {@code obdx.event_scores} row per competitor, on this exercise id, <em>is</em> the competitor's total. The
 * classification reads that value straight as the total score and skips the per-exercise arithmetic entirely
 * (see {@code GetObdxClassificationServiceCase}); everything derived from the total — qualification,
 * rank score, position, tie tiers — keeps working unchanged, because those depend on the event's
 * {@code configurationId}, not on the exercise.
 *
 * <p>The id is deliberately outside the {@code OBDX.<FEDERATION>_<GRADE>.<n>} namespace of the federation
 * configurations: it belongs to no grade and no configuration file lists it. Consequently it has no coefficient
 * and no {@code allowed_values} scale — the score is a total, not a 0–10 mark — so the maximum attainable total
 * is derived from every exercise of the event's configuration rather than from the event's own exercise rows.
 */
public final class ObdxFinalScoreExercise {

    /** Exercise id of the static final score. Version-agnostic; {@link #isFinalScore} also accepts {@code _Vn}. */
    public static final String EXERCISE_ID = "OBDX.FINAL_SCORE";

    /**
     * Judge that owns imported score rows. An import knows the total but not who marked what, and
     * {@code obdx.event_scores.judge_id} is both {@code NOT NULL} and part of the primary key, so imported rows
     * are attributed to this seeded judge. It is never rendered: the classification ignores the judge of a
     * final-score row.
     */
    public static final String UNKNOWN_JUDGE_ID = "UNKNOWN";

    /** Trailing version token of an exercise id, e.g. the {@code _V0} in {@code OBDX.FINAL_SCORE_V0}. */
    private static final Pattern VERSION_SUFFIX = Pattern.compile("_V\\d+$");

    private ObdxFinalScoreExercise() {
    }

    /** Whether the given exercise id is the final-score exercise, ignoring any trailing version suffix. */
    public static boolean isFinalScore(String exerciseId) {
        if (exerciseId == null) {
            return false;
        }
        return EXERCISE_ID.equals(VERSION_SUFFIX.matcher(exerciseId).replaceFirst(""));
    }
}
