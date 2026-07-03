package com.k9x.infrastructure.in.rest.endpoints.secured.breeds;

import com.k9x.application.breeds.use_case.GetBreedListServiceCase;
import com.k9x.oas.stub.api.SecuredBreedsFetchAllApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class FetchBreeds implements SecuredBreedsFetchAllApiDelegate {

    private final GetBreedListServiceCase getBreedListServiceCase;

    public FetchBreeds(GetBreedListServiceCase getBreedListServiceCase) {
        this.getBreedListServiceCase = getBreedListServiceCase;
    }

    @Override
    public ResponseEntity<List<IdNameDTO>> fetchBreedsSecured() {
        return ResponseEntity.ok(
                getBreedListServiceCase.getBreeds().stream()
                        .map(breed -> new IdNameDTO(breed.name(), breed.id()))
                        .toList()
        );
    }
}
