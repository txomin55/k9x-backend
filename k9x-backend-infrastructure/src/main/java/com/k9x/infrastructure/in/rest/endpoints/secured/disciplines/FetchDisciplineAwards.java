package com.k9x.infrastructure.in.rest.endpoints.secured.disciplines;

import com.k9x.application.awards.use_case.GetAwardListServiceCase;
import com.k9x.oas.stub.api.SecuredDisciplinesFetchAwardsApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class FetchDisciplineAwards implements SecuredDisciplinesFetchAwardsApiDelegate {

    private final GetAwardListServiceCase getAwardListServiceCase;

    public FetchDisciplineAwards(GetAwardListServiceCase getAwardListServiceCase) {
        this.getAwardListServiceCase = getAwardListServiceCase;
    }

    @Override
    public ResponseEntity<List<IdNameDTO>> fetchDisciplineAwardsSecured(String id) {
        return ResponseEntity.ok(
                getAwardListServiceCase.getAwards(id).stream()
                        .map(award -> new IdNameDTO(award.name(), award.id()))
                        .toList()
        );
    }
}
