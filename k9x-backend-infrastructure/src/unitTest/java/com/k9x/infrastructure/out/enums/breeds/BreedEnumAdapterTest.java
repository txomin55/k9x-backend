package com.k9x.infrastructure.out.enums.breeds;

import com.k9x.application.breeds.use_case.dto.BreedDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BreedEnumAdapterTest {

    private BreedEnumAdapter adapter;

    @BeforeEach
    void setUp() {
        MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), isNull(), anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(2));
        adapter = new BreedEnumAdapter(messageSource);
    }

    @Test
    void returns_one_entry_per_enum_constant() {
        List<BreedDTO> result = adapter.getBreeds();

        assertThat(result).hasSize(Breed.values().length);
    }

    @Test
    void id_matches_enum_constant_name() {
        List<BreedDTO> result = adapter.getBreeds();

        assertThat(result).extracting(BreedDTO::id)
                .containsExactlyInAnyOrderElementsOf(Arrays.stream(Breed.values()).map(Enum::name).toList());
    }

    @Test
    void name_falls_back_to_enum_name_when_no_translation() {
        List<BreedDTO> result = adapter.getBreeds();

        assertThat(result).allSatisfy(breed -> assertThat(breed.name()).isEqualTo(breed.id()));
    }
}
