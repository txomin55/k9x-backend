package com.k9x.domain.disciplines.obdx;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The "class" (clase) a competitor works in, as printed on the paper working booklet: a bare grade number.
 *
 * <p>There is no class column anywhere — the class is encoded in the configuration id
 * ({@code OBDX.FCI_GRADE_3.V0}, {@code OBDX.RSCE_GRADO_1.V0}), which is why this lives here as a domain rule
 * instead of being pattern-matched at the edge. Configurations with no numeric grade
 * ({@code DEBUTANTE}, {@code PRE_DEBUTANTI}, {@code COBS}) legitimately have no number; callers fall back to
 * the configuration's translated name for those.
 */
public final class ObdxConfigurationGrade {

    /**
     * Matches the grade digits of the segment naming the class, in either language used by the federations'
     * ids ({@code GRADE_3} / {@code GRADO_1}). Anchored to a word boundary so a version suffix like
     * {@code .V0} can never be read as a grade.
     */
    private static final Pattern GRADE = Pattern.compile("GRAD[EO]_(\\d+)");

    private ObdxConfigurationGrade() {
    }

    /**
     * @return the grade number of the configuration, or {@code null} when the configuration has none.
     */
    public static String resolve(String configurationId) {
        if (configurationId == null) {
            return null;
        }
        Matcher matcher = GRADE.matcher(configurationId.toUpperCase());
        return matcher.find() ? matcher.group(1) : null;
    }
}
