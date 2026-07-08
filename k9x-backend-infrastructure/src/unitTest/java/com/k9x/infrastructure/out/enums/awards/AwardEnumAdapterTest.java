package com.k9x.infrastructure.out.enums.awards;

import com.k9x.domain.disciplines.exceptions.DisciplineNotFoundException;
import com.k9x.application.awards.use_case.dto.AwardDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AwardEnumAdapterTest {

    private AwardEnumAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AwardEnumAdapter();
    }

    @Test
    void returns_one_entry_per_obdx_award_enum_constant() {
        List<AwardDTO> result = adapter.getAwards("OBDX");

        assertThat(result).hasSize(ObdxAward.values().length);
    }

    @Test
    void id_matches_enum_constant_name_and_name_is_capitalized() {
        List<AwardDTO> result = adapter.getAwards("OBDX");

        assertThat(result).containsExactlyInAnyOrder(
                new AwardDTO("CACOB", "Cacob"),
                new AwardDTO("CACIOB", "Caciob"));
    }

    @Test
    void throws_discipline_not_found_when_discipline_case_does_not_match() {
        assertThatThrownBy(() -> adapter.getAwards("obdx"))
                .isInstanceOf(DisciplineNotFoundException.class);
    }

    @Test
    void throws_discipline_not_found_when_discipline_is_unknown() {
        assertThatThrownBy(() -> adapter.getAwards("NOT_A_DISCIPLINE"))
                .isInstanceOf(DisciplineNotFoundException.class);
    }

    @Test
    void throws_discipline_not_found_when_discipline_is_null() {
        assertThatThrownBy(() -> adapter.getAwards(null))
                .isInstanceOf(DisciplineNotFoundException.class);
    }
}
