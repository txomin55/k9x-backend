package com.k9x.infrastructure.out.enums.categories;

import com.k9x.application.categories.port.GetEventCategoryListPort;
import com.k9x.application.categories.use_case.dto.EventCategoryDTO;
import com.k9x.domain.disciplines.obdx.ObdxEventCategory;
import com.k9x.domain.disciplines.valueobjects.Discipline;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Arrays;
import java.util.List;

/**
 * Exposes the event categories as a selectable catalogue. The enum lives in the domain module — a category is
 * persisted with the event — so this adapter only walks its values and resolves the label, keeping i18n out of
 * the application layer.
 */
public class EventCategoryEnumAdapter implements GetEventCategoryListPort {

    private final MessageSource messageSource;

    public EventCategoryEnumAdapter(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public List<EventCategoryDTO> getCategories(String disciplineId) {
        Discipline discipline = Discipline.fromRequest(disciplineId);
        return switch (discipline) {
            case OBDX -> Arrays.stream(ObdxEventCategory.values())
                    .map(category -> new EventCategoryDTO(category.name(), translate(category)))
                    .toList();
        };
    }

    private String translate(ObdxEventCategory category) {
        String key = "event.category." + category.name().toLowerCase() + ".name";
        return messageSource.getMessage(key, null, category.name(), LocaleContextHolder.getLocale());
    }
}
