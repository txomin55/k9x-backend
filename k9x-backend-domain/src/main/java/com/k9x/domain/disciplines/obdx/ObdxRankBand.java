package com.k9x.domain.disciplines.obdx;

import java.util.regex.Pattern;

/**
 * The ranking-score band {@code [min, max]} each OBDX configuration occupies within the global 0–1000 scale.
 * An event's automatic {@code rank_score} is placed inside its configuration's band from the competitor count
 * and the international flag (see {@link ObdxRank#score(int, int, boolean)}).
 *
 * <p>The band is a fixed ranking rule that does not change between configuration <em>versions</em>, so it lives
 * here in code (keyed by the version-independent configuration id) rather than being duplicated across every
 * {@code configuration.json} version. Every {@code max} stays at or below {@link ObdxRank#MAX_AUTOMATIC_SCORE},
 * leaving the top slice for the manually seeded A++ rank.
 */
public enum ObdxRankBand {
    RSCE_DEBUTANTE("OBDX_RSCE_DEBUTANTE", 100, 200),
    CPC_COBS("CPC_COBS", 100, 200),
    FCI_GRADE_1("OBDX_FCI_GRADE_1", 201, 400),
    RSCE_GRADE_1("OBDX_RSCE_GRADE_1", 201, 400),
    FCI_GRADE_2("OBDX_FCI_GRADE_2", 401, 600),
    FCI_GRADE_3("OBDX_FCI_GRADE_3", 601, 950);

    /** Strips the trailing {@code .V<n>} version suffix, e.g. {@code OBDX_FCI_GRADE_3.V0 -> OBDX_FCI_GRADE_3}. */
    private static final Pattern VERSION_SUFFIX = Pattern.compile("\\.V\\d+$");

    private final String configurationKey;
    private final int min;
    private final int max;

    ObdxRankBand(String configurationKey, int min, int max) {
        this.configurationKey = configurationKey;
        this.min = min;
        this.max = max;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    /**
     * Resolves the band for a configuration id (with or without its {@code .V<n>} version suffix), or
     * {@code null} when the configuration has no band defined.
     */
    public static ObdxRankBand fromConfigurationId(String configurationId) {
        if (configurationId == null) {
            return null;
        }
        String key = VERSION_SUFFIX.matcher(configurationId).replaceFirst("");
        for (ObdxRankBand band : values()) {
            if (band.configurationKey.equals(key)) {
                return band;
            }
        }
        return null;
    }
}
