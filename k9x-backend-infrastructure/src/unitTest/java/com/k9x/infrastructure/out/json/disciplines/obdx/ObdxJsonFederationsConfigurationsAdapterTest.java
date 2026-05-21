package com.k9x.infrastructure.out.json.disciplines.obdx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.disciplines.obdx.use_case.dto.ObdxConfigurationsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ObdxJsonFederationsConfigurationsAdapterTest {

    private ObdxJsonFederationsConfigurationsAdapter adapter;

    @BeforeEach
    void setUp() {
        MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), isNull(), anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(2));
        adapter = new ObdxJsonFederationsConfigurationsAdapter(new ObjectMapper(), messageSource);
    }

    @Test
    void returns_configurations_for_all_federations() throws IOException {
        List<ObdxConfigurationsDTO> result = adapter.getConfigurations();

        assertThat(result).extracting(c -> c.info().id())
                .containsExactlyInAnyOrder("FCI", "RSCE");
    }

    @Test
    void federation_id_is_uppercase() throws IOException {
        List<ObdxConfigurationsDTO> result = adapter.getConfigurations();

        assertThat(result).allSatisfy(config ->
                assertThat(config.info().id()).isEqualTo(config.info().id().toUpperCase()));
    }

    @Test
    void each_federation_has_configurations_with_exercises() throws IOException {
        List<ObdxConfigurationsDTO> result = adapter.getConfigurations();

        assertThat(result).allSatisfy(federation -> {
            assertThat(federation.configurations()).isNotEmpty();
            federation.configurations().forEach(config ->
                    assertThat(config.exercises()).isNotEmpty());
        });
    }

    @Test
    void exercise_names_fall_back_to_id_when_no_translation() throws IOException {
        List<ObdxConfigurationsDTO> result = adapter.getConfigurations();

        result.forEach(federation ->
                federation.configurations().forEach(config ->
                        config.exercises().forEach(exercise ->
                                assertThat(exercise.name()).isEqualTo(exercise.id()))));
    }
}