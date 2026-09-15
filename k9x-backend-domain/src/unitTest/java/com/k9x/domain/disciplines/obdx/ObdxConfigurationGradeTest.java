package com.k9x.domain.disciplines.obdx;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ObdxConfigurationGradeTest {

    @Test
    void resolves_the_grade_of_an_fci_configuration() {
        assertEquals("3", ObdxConfigurationGrade.resolve("OBDX.FCI_GRADE_3.V2022"));
    }

    /** RSCE spells the segment in Spanish, so both spellings must resolve. */
    @Test
    void resolves_the_grade_of_a_spanish_spelled_configuration() {
        assertEquals("1", ObdxConfigurationGrade.resolve("OBDX.RSCE_GRADO_1.V2026"));
    }

    /** NKK numbers its classes too, spelled KLASSE in Norwegian. */
    @Test
    void resolves_the_grade_of_a_norwegian_spelled_configuration() {
        assertEquals("1", ObdxConfigurationGrade.resolve("OBDX.NKN_KLASSE_1.V2016"));
        assertEquals("3", ObdxConfigurationGrade.resolve("OBDX.NKN_KLASSE_3.V2016"));
    }

    /** SKK numbers its classes too, spelled KLASS in Swedish, with no trailing E. */
    @Test
    void resolves_the_grade_of_a_swedish_spelled_configuration() {
        assertEquals("1", ObdxConfigurationGrade.resolve("OBDX.SKK_KLASS_1.V2016"));
        assertEquals("2", ObdxConfigurationGrade.resolve("OBDX.SKK_KLASS_2.V2016"));
    }

    @Test
    void has_no_grade_for_configurations_without_a_number() {
        assertNull(ObdxConfigurationGrade.resolve("OBDX.RSCE_DEBUTANTE.V2022"));
        assertNull(ObdxConfigurationGrade.resolve("OBDX.ENCI_PRE_DEBUTANTI.V2022"));
        assertNull(ObdxConfigurationGrade.resolve("OBDX.CPC_COBS.V2016"));
        assertNull(ObdxConfigurationGrade.resolve("OBDX.SPKL_ALO.V2016"));
    }

    /** The version suffix is digits too; reading it as the grade would print "0" for every class. */
    @Test
    void never_reads_the_version_suffix_as_the_grade() {
        assertNull(ObdxConfigurationGrade.resolve("OBDX.CPC_COBS.V2016"));
        assertEquals("2", ObdxConfigurationGrade.resolve("OBDX.FCI_GRADE_2.V2022"));
    }

    @Test
    void has_no_grade_for_a_null_configuration() {
        assertNull(ObdxConfigurationGrade.resolve(null));
    }
}
