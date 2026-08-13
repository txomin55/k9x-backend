package com.k9x.application.categories.use_case;

import com.k9x.application.categories.port.GetEventCategoryListPort;
import com.k9x.application.categories.use_case.dto.EventCategoryDTO;

import java.util.List;

public class GetEventCategoryListServiceCase {

    private final GetEventCategoryListPort getEventCategoryListPort;

    public GetEventCategoryListServiceCase(GetEventCategoryListPort getEventCategoryListPort) {
        this.getEventCategoryListPort = getEventCategoryListPort;
    }

    public List<EventCategoryDTO> getCategories(String disciplineId) {
        return getEventCategoryListPort.getCategories(disciplineId);
    }
}
