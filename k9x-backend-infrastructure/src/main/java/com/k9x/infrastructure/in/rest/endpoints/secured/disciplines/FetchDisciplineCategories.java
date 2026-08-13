package com.k9x.infrastructure.in.rest.endpoints.secured.disciplines;

import com.k9x.application.categories.use_case.GetEventCategoryListServiceCase;
import com.k9x.oas.stub.api.SecuredDisciplinesFetchCategoriesApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class FetchDisciplineCategories implements SecuredDisciplinesFetchCategoriesApiDelegate {

    private final GetEventCategoryListServiceCase getEventCategoryListServiceCase;

    public FetchDisciplineCategories(GetEventCategoryListServiceCase getEventCategoryListServiceCase) {
        this.getEventCategoryListServiceCase = getEventCategoryListServiceCase;
    }

    @Override
    public ResponseEntity<List<IdNameDTO>> fetchDisciplineCategoriesSecured(String id) {
        return ResponseEntity.ok(
                getEventCategoryListServiceCase.getCategories(id).stream()
                        .map(category -> new IdNameDTO(category.name(), category.id()))
                        .toList()
        );
    }
}
