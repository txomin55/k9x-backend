package com.k9x.domain.disciplines.obdx;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ObdxConfigurationGradeTest {

    @Test
    void resolves_the_grade_of_an_fci_configuration() {
        assertEquals("3", ObdxConfigurationGrade.resolve("OBDX.FCI_GRADE_3.V0"));
    }

    /** RSCE spells the segment in Spanish, so both spellings must resolve. */
    @Test
    void resolves_the_grade_of_a_spanish_spelled_configuration() {
        assertEquals("1", ObdxConfigurationGrade.resolve("OBDX.RSCE_GRADO_1.V0"));
    }

    @Test
    void has_no_grade_for_configurations_without_a_number() {
        assertNull(ObdxConfigurationGrade.resolve("OBDX.RSCE_DEBUTANTE.V0"));
        assertNull(ObdxConfigurationGrade.resolve("OBDX.ENCI_PRE_DEBUTANTI.V0"));
        assertNull(ObdxConfigurationGrade.resolve("OBDX.CPC_COBS.V0"));
    }

    /** The version suffix is digits too; reading it as the grade would print "0" for every class. */
    @Test
    void never_reads_the_version_suffix_as_the_grade() {
        assertNull(ObdxConfigurationGrade.resolve("OBDX.CPC_COBS.V0"));
        assertEquals("2", ObdxConfigurationGrade.resolve("OBDX.FCI_GRADE_2.V0"));
    }

    @Test
    void has_no_grade_for_a_null_configuration() {
        assertNull(ObdxConfigurationGrade.resolve(null));
    }
}
