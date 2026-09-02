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
     * The single anonymous judge. Owns imported score rows — an import knows the total but not who marked
     * what — and, more generally, any score whose author the source does not name when there is only
     * <em>one</em> mark to place. {@code obdx.event_scores.judge_id} is both {@code NOT NULL} and part of the
     * primary key, so such rows need some judge, and this one is seeded by the database.
     */
    public static final String UNKNOWN_JUDGE_ID = "UNKNOWN";

    /**
     * An anonymous judge: {@code UNKNOWN}, or {@code UNKNOWN_1}, {@code UNKNOWN_2}… for the numbered slots.
     *
     * <p>The numbered ones exist for the source that publishes <em>several</em> marks per exercise without
     * saying which judge gave each: there are N marks to place and no names to place them under, so they go to
     * N anonymous slots and the average comes out right. One mark and no name is the plain {@code UNKNOWN};
     * that also covers a panel of four that only publishes one figure per exercise, because there is still a
     * single mark to attribute.
     *
     * <p>None of them is a person, so <strong>none belongs in {@code obdx.event_judges}</strong>: that table is
     * the jury of the trial and keeps carrying the real names. They appear only as the author of a score, and
     * the classification resolves them on the fly instead of looking them up in the panel — see
     * {@code GetObdxClassificationServiceCase}.
     */
    private static final Pattern UNKNOWN_JUDGE = Pattern.compile("^UNKNOWN(?:_\\d+)?$");

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

    /** Whether the given judge id is one of the anonymous judges. See {@link #UNKNOWN_JUDGE}. */
    public static boolean isUnknownJudge(String judgeId) {
        return judgeId != null && UNKNOWN_JUDGE.matcher(judgeId).matches();
    }
}
