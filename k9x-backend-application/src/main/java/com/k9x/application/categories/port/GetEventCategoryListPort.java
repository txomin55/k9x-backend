package com.k9x.application.categories.port;

import com.k9x.application.categories.use_case.dto.EventCategoryDTO;

import java.util.List;

public interface GetEventCategoryListPort {

    List<EventCategoryDTO> getCategories(String disciplineId);
}
