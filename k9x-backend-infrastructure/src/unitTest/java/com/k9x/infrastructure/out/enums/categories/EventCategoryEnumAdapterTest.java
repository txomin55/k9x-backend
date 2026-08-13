package com.k9x.infrastructure.out.enums.categories;

import com.k9x.application.categories.use_case.dto.EventCategoryDTO;
import com.k9x.domain.disciplines.exceptions.DisciplineNotFoundException;
import com.k9x.domain.disciplines.obdx.ObdxEventCategory;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventCategoryEnumAdapterTest {

    private static MessageSource fallingBackToTheDefault() {
        MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), isNull(), anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(2));
        return messageSource;
    }

    @Test
    void returns_one_entry_per_enum_constant() {
        List<EventCategoryDTO> result =
                new EventCategoryEnumAdapter(fallingBackToTheDefault()).getCategories("OBDX");

        assertThat(result).extracting(EventCategoryDTO::id)
                .containsExactlyElementsOf(Arrays.stream(ObdxEventCategory.values()).map(Enum::name).toList());
    }

    @Test
    void name_falls_back_to_the_enum_name_when_there_is_no_translation() {
        List<EventCategoryDTO> result =
                new EventCategoryEnumAdapter(fallingBackToTheDefault()).getCategories("OBDX");

        assertThat(result).allSatisfy(category -> assertThat(category.name()).isEqualTo(category.id()));
    }

    @Test
    void resolves_the_label_with_the_expected_message_key() {
        MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), isNull(), anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(2));
        when(messageSource.getMessage(eq("event.category.wc_semi.name"), isNull(), anyString(), any()))
                .thenReturn("Semifinal WC");

        List<EventCategoryDTO> result = new EventCategoryEnumAdapter(messageSource).getCategories("OBDX");

        assertThat(result).filteredOn(category -> category.id().equals("WC_SEMI"))
                .extracting(EventCategoryDTO::name)
                .containsExactly("Semifinal WC");
    }

    @Test
    void throws_discipline_not_found_when_discipline_is_unknown() {
        EventCategoryEnumAdapter adapter = new EventCategoryEnumAdapter(fallingBackToTheDefault());

        assertThatThrownBy(() -> adapter.getCategories("NOT_A_DISCIPLINE"))
                .isInstanceOf(DisciplineNotFoundException.class);
    }
}
