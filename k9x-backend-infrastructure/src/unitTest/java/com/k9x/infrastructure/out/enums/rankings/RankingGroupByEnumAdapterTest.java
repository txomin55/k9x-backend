package com.k9x.infrastructure.out.enums.rankings;

import com.k9x.application.rankings.use_case.dto.RankingCriterionDTO;
import com.k9x.domain.rankings.RankingGroupBy;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RankingGroupByEnumAdapterTest {

    private static MessageSource fallingBackToTheDefault() {
        MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), isNull(), anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(2));
        return messageSource;
    }

    @Test
    void returns_one_entry_per_enum_constant() {
        List<RankingCriterionDTO> result =
                new RankingGroupByEnumAdapter(fallingBackToTheDefault()).getGroupBys();

        assertThat(result).extracting(RankingCriterionDTO::id)
                .containsExactlyElementsOf(Arrays.stream(RankingGroupBy.values()).map(Enum::name).toList());
    }

    @Test
    void name_falls_back_to_the_enum_name_when_there_is_no_translation() {
        List<RankingCriterionDTO> result =
                new RankingGroupByEnumAdapter(fallingBackToTheDefault()).getGroupBys();

        assertThat(result).allSatisfy(criterion -> assertThat(criterion.name()).isEqualTo(criterion.id()));
    }

    @Test
    void resolves_the_label_with_the_expected_message_key() {
        MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(eq("ranking.group_by.individual.name"), isNull(), anyString(), any()))
                .thenReturn("Individual");
        when(messageSource.getMessage(eq("ranking.group_by.team.name"), isNull(), anyString(), any()))
                .thenReturn("Equipo");
        when(messageSource.getMessage(eq("ranking.group_by.country.name"), isNull(), anyString(), any()))
                .thenReturn("Pais");

        List<RankingCriterionDTO> result = new RankingGroupByEnumAdapter(messageSource).getGroupBys();

        assertThat(result).extracting(RankingCriterionDTO::name)
                .containsExactly("Individual", "Equipo", "Pais");
    }
}
