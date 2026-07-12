package com.k9x.domain.disciplines.obdx;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * OBDX exercises that must not gate a competitor's LIVE/SETTLED status, as configured in each
 * federation's {@code configuration.json}. Two kinds live here:
 *
 * <ul>
 *   <li><b>Group stays</b> — scored for the whole field at once. Their score must not, on its own, flip
 *       a competitor to LIVE: scoring the group flight would otherwise mark everyone live before (or
 *       after) their individual runs.</li>
 *   <li><b>General impression</b> — the closing overall score, given collectively at the very end. An
 *       unscored general impression must not keep a competitor that has already finished its individual
 *       runs stuck on LIVE.</li>
 * </ul>
 *
 * <p>Consequently these exercises are ignored both when deciding whether a competitor has started and
 * whether it is settled (see {@code EventSnapshot}): only individual exercises drive the status.
 *
 * <p>Each constant holds the <em>version-agnostic</em> exercise id, i.e. the config id without its
 * trailing version suffix ({@code _V0}, {@code _V1}, …). {@link #isExcluded(String)} strips that suffix
 * before matching, so a config version bump ({@code _V0} → {@code _V1}) needs no change here.
 */
public enum LiveExcludedExercise {
    // Group stays
    CPC_COBS_7("OBDX_CPC_COBS.7"),
    RSCE_GRADE_1_1("OBDX_RSCE_GRADE_1.1"),
    FCI_GRADE_3_1("OBDX_FCI_GRADE_3.1"),
    FCI_GRADE_3_2("OBDX_FCI_GRADE_3.2"),
    FCI_GRADE_2_1("OBDX_FCI_GRADE_2.1"),
    // General impression
    RSCE_DEBUTANTE_9("OBDX_RSCE_DEBUTANTE.9"),
    FCI_GRADE_1_9("OBDX_FCI_GRADE_1.9"),
    FCI_GRADE_2_10("OBDX_FCI_GRADE_2.10");

    /** Trailing version token of an exercise id, e.g. the {@code _V0} in {@code OBDX_FCI_GRADE_2.1_V0}. */
    private static final Pattern VERSION_SUFFIX = Pattern.compile("_V\\d+$");

    private final String baseId;

    LiveExcludedExercise(String baseId) {
        this.baseId = baseId;
    }

    public String baseId() {
        return baseId;
    }

    private static final Set<String> BASE_IDS = Arrays.stream(values())
            .map(LiveExcludedExercise::baseId)
            .collect(Collectors.toUnmodifiableSet());

    public static boolean isExcluded(String exerciseId) {
        if (exerciseId == null) {
            return false;
        }
        return BASE_IDS.contains(VERSION_SUFFIX.matcher(exerciseId).replaceFirst(""));
    }
}
