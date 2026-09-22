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
 * trailing version suffix ({@code _V2022}, {@code _V1}, …). {@link #isExcluded(String)} strips that suffix
 * before matching, so a config version bump ({@code _V2022} → {@code _V1}) needs no change here.
 */
public enum LiveExcludedExercise {
    // Group stays
    CPC_COBS_7("OBDX.CPC_COBS.7"),
    RSCE_GRADO_1_1("OBDX.RSCE_GRADO_1.1"),
    FCI_GRADE_3_1("OBDX.FCI_GRADE_3.1"),
    FCI_GRADE_3_2("OBDX.FCI_GRADE_3.2"),
    FCI_GRADE_2_1("OBDX.FCI_GRADE_2.1"),
    FCI_GRADE_1_1("OBDX.FCI_GRADE_1.1"),
    SPKL_ALO_1("OBDX.SPKL_ALO.1"),
    SCC_BREVET_3("OBDX.SCC_BREVET.3"),
    NKN_KLASSE_1_2("OBDX.NKN_KLASSE_1.2"),
    NKN_KLASSE_2_1("OBDX.NKN_KLASSE_2.1"),
    NKN_KLASSE_3_1("OBDX.NKN_KLASSE_3.1"),
    SKK_KLASS_1_8("OBDX.SKK_KLASS_1.8"),
    SKK_KLASS_2_9("OBDX.SKK_KLASS_2.9"),
    VDH_BEGINNER_2("OBDX.VDH_BEGINNER.2"),
    VDH_SENIOR_1("OBDX.VDH_SENIOR.1"),
    OKV_BEGINNER_2("OBDX.OKV_BEGINNER.2"),
    OKV_SENIOR_1("OBDX.OKV_SENIOR.1"),
    // The Danish group stay moves slot between rulebook versions —8 in V2020, 9 in V2023, 1 from V2026,
    // and split into a sit half and a down half from V2024— so every slot it has ever occupied is listed.
    // These ids carry no version suffix, so a slot listed here is excluded in EVERY version: in klasse 1
    // that also takes out .1 (heel on leash before V2026) and .8 (sit for 15 seconds from V2023). Both are
    // individual exercises, so those two versions lose a little of what could drive LIVE; the alternative
    // —letting the group stay flip the whole field to LIVE— is the failure this enum exists to prevent.
    DKK_KLASSE_1_1("OBDX.DKK_KLASSE_1.1"),
    DKK_KLASSE_1_8("OBDX.DKK_KLASSE_1.8"),
    DKK_KLASSE_1_9("OBDX.DKK_KLASSE_1.9"),
    DKK_KLASSE_2_1("OBDX.DKK_KLASSE_2.1"),
    DKK_KLASSE_2_2("OBDX.DKK_KLASSE_2.2"),
    DKK_KLASSE_3_1("OBDX.DKK_KLASSE_3.1"),
    DKK_KLASSE_3_2("OBDX.DKK_KLASSE_3.2"),
    // General impression
    CPC_COBS_8("OBDX.CPC_COBS.8"),
    RSCE_DEBUTANTE_9("OBDX.RSCE_DEBUTANTE.9"),
    FCI_GRADE_1_9("OBDX.FCI_GRADE_1.9"),
    FCI_GRADE_2_10("OBDX.FCI_GRADE_2.10"),
    SPKL_ALO_8("OBDX.SPKL_ALO.8"),
    SCC_BREVET_11("OBDX.SCC_BREVET.11"),
    NKN_KLASSE_1_9("OBDX.NKN_KLASSE_1.9"),
    NKN_KLASSE_2_9("OBDX.NKN_KLASSE_2.9"),
    NKN_KLASSE_3_10("OBDX.NKN_KLASSE_3.10"),
    SKK_STARTKLASS_7("OBDX.SKK_STARTKLASS.7"),
    SKK_KLASS_1_9("OBDX.SKK_KLASS_1.9"),
    SKK_KLASS_2_10("OBDX.SKK_KLASS_2.10"),
    VDH_BEGINNER_11("OBDX.VDH_BEGINNER.11"),
    VDH_SENIOR_10("OBDX.VDH_SENIOR.10"),
    OKV_BEGINNER_9("OBDX.OKV_BEGINNER.9"),
    OKV_SENIOR_9("OBDX.OKV_SENIOR.9"),
    DKK_KLASSE_1_10("OBDX.DKK_KLASSE_1.10"),
    DKK_KLASSE_2_11("OBDX.DKK_KLASSE_2.11"),
    DKK_KLASSE_2_12("OBDX.DKK_KLASSE_2.12"),
    DKK_KLASSE_3_11("OBDX.DKK_KLASSE_3.11");

    /**
     * Trailing version token of an exercise id, e.g. the {@code _V2022} in {@code OBDX.FCI_GRADE_2.1_V2022}.
     */
    private static final Pattern VERSION_SUFFIX = Pattern.compile("_V\\d+$");
    private static final Set<String> BASE_IDS = Arrays.stream(values())
            .map(LiveExcludedExercise::baseId)
            .collect(Collectors.toUnmodifiableSet());
    private final String baseId;

    LiveExcludedExercise(String baseId) {
        this.baseId = baseId;
    }

    public static boolean isExcluded(String exerciseId) {
        if (exerciseId == null) {
            return false;
        }
        return BASE_IDS.contains(VERSION_SUFFIX.matcher(exerciseId).replaceFirst(""));
    }

    public String baseId() {
        return baseId;
    }
}
