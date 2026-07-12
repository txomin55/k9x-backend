package com.k9x.domain.disciplines.obdx;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * OBDX exercises that are scored for every competitor at once (group stays), as configured in each
 * federation's {@code configuration.json}. A group exercise's score must not, on its own, flip a
 * competitor to LIVE: when the group flight is scored the whole field gets a score at the same time,
 * which would otherwise mark everyone live before (or after) their individual runs. Only a score on a
 * non-group exercise starts a competitor. See {@code EventSnapshot#isCompetitorStarted}.
 *
 * <p>Each constant holds the <em>version-agnostic</em> exercise id, i.e. the config id without its
 * trailing version suffix ({@code _V0}, {@code _V1}, …). {@link #isGroupExercise(String)} strips that
 * suffix before matching, so a config version bump ({@code _V0} → {@code _V1}) needs no change here.
 */
public enum GroupExercise {
    CPC_COBS_7("OBDX_CPC_COBS.7"),
    RSCE_GRADE_1_1("OBDX_RSCE_GRADE_1.1"),
    FCI_GRADE_3_1("OBDX_FCI_GRADE_3.1"),
    FCI_GRADE_3_2("OBDX_FCI_GRADE_3.2"),
    FCI_GRADE_2_1("OBDX_FCI_GRADE_2.1");

    /** Trailing version token of an exercise id, e.g. the {@code _V0} in {@code OBDX_FCI_GRADE_2.1_V0}. */
    private static final Pattern VERSION_SUFFIX = Pattern.compile("_V\\d+$");

    private final String baseId;

    GroupExercise(String baseId) {
        this.baseId = baseId;
    }

    public String baseId() {
        return baseId;
    }

    private static final Set<String> BASE_IDS = Arrays.stream(values())
            .map(GroupExercise::baseId)
            .collect(Collectors.toUnmodifiableSet());

    public static boolean isGroupExercise(String exerciseId) {
        if (exerciseId == null) {
            return false;
        }
        return BASE_IDS.contains(VERSION_SUFFIX.matcher(exerciseId).replaceFirst(""));
    }
}
