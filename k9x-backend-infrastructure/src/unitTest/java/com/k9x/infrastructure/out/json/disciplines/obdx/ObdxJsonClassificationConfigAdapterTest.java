package com.k9x.infrastructure.out.json.disciplines.obdx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.events.obdx.use_case.dto.ObdxClassificationConfigDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class ObdxJsonClassificationConfigAdapterTest {

    private ObdxJsonClassificationConfigAdapter adapter;

    @BeforeEach
    void setUp() {
        ObdxFederationsConfigurationsCache cache = new ObdxFederationsConfigurationsCache(new ObjectMapper());
        adapter = new ObdxJsonClassificationConfigAdapter(cache);
    }

    @Test
    void loads_qualification_scale_for_grades() {
        ObdxClassificationConfigDTO config = adapter.getConfig("OBDX_RSCE_DEBUTANTE_V0");

        assertThat(config.qualifications())
                .extracting(ObdxClassificationConfigDTO.QualificationThreshold::id,
                        ObdxClassificationConfigDTO.QualificationThreshold::minScore)
                .containsExactly(
                        tuple("EXC", new java.math.BigDecimal("256")),
                        tuple("MB", new java.math.BigDecimal("224")),
                        tuple("B", new java.math.BigDecimal("192")));
    }

    @Test
    void loads_qualification_scale_for_cobs() {
        ObdxClassificationConfigDTO config = adapter.getConfig("CPC_COBS_V0");

        assertThat(config.qualifications())
                .extracting(ObdxClassificationConfigDTO.QualificationThreshold::id)
                .containsExactly("EXC", "MB", "B");
    }
}
