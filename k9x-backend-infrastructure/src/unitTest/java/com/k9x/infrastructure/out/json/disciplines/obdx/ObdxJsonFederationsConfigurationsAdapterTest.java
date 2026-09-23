package com.k9x.infrastructure.out.json.disciplines.obdx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.disciplines.use_case.dto.ConfigurationDTO;
import com.k9x.application.disciplines.use_case.dto.ConfigurationsDTO;
import com.k9x.infrastructure.out.json.disciplines.obdx.dto.FederationConfigurationFileDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ObdxJsonFederationsConfigurationsAdapterTest {

    private ObdxJsonFederationsConfigurationsAdapter adapter;
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), isNull(), anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(2));
        ObdxFederationsConfigurationsCache cache = new ObdxFederationsConfigurationsCache(new ObjectMapper());
        adapter = new ObdxJsonFederationsConfigurationsAdapter(cache, messageSource);
    }

    @Test
    void returns_configurations_for_all_federations() {
        List<ConfigurationsDTO> result = adapter.getConfigurations();

        assertThat(result).extracting(c -> c.info().id())
                .containsExactlyInAnyOrder("CPC", "DKK", "ENCI", "FCI", "NKN", "OKV", "RSCE", "SCC", "SKK", "SPKL", "VDH");
    }

    @Test
    void federation_id_is_uppercase() {
        List<ConfigurationsDTO> result = adapter.getConfigurations();

        assertThat(result).allSatisfy(config ->
                assertThat(config.info().id()).isEqualTo(config.info().id().toUpperCase()));
    }

    @Test
    void each_federation_has_configurations_with_exercises() {
        List<ConfigurationsDTO> result = adapter.getConfigurations();

        assertThat(result).allSatisfy(federation -> {
            assertThat(federation.configurations()).isNotEmpty();
            federation.configurations().forEach(config ->
                    assertThat(config.exercises()).isNotEmpty());
        });
    }

    @Test
    void exercise_names_fall_back_to_id_when_no_translation() {
        List<ConfigurationsDTO> result = adapter.getConfigurations();

        result.forEach(federation ->
                federation.configurations().forEach(config ->
                        config.exercises().forEach(exercise ->
                                assertThat(exercise.name()).isEqualTo(exercise.id()))));
    }

    @Test
    void returns_only_the_current_version_of_each_class() {
        adapter = new ObdxJsonFederationsConfigurationsAdapter(cacheOf(
                entry("fci", "grade_2", 2016),
                entry("fci", "grade_2", 2022),
                entry("fci", "grade_2", 2021),
                entry("fci", "grade_3", 2021),
                entry("fci", "grade_3", 2016)), messageSource);

        List<ConfigurationsDTO> result = adapter.getConfigurations();

        assertThat(result).singleElement()
                .satisfies(federation -> assertThat(federation.configurations()).extracting(ConfigurationDTO::id)
                        .containsExactly("OBDX_FCI_GRADE_2_V2022", "OBDX_FCI_GRADE_3_V2021"));
    }

    private static ObdxFederationsConfigurationsCache cacheOf(ObdxFederationsConfigurationsCache.Entry... entries) {
        return new ObdxFederationsConfigurationsCache(List.of(entries));
    }

    private static ObdxFederationsConfigurationsCache.Entry entry(String federation, String classKey, int version) {
        String id = "OBDX_%s_%s_V%d".formatted(federation.toUpperCase(), classKey.toUpperCase(), version);
        return new ObdxFederationsConfigurationsCache.Entry(federation, classKey, version,
                new FederationConfigurationFileDTO(id, "EU", null, null, null,
                        List.of(new FederationConfigurationFileDTO.Exercise(id + ".1", null)), null));
    }
}
