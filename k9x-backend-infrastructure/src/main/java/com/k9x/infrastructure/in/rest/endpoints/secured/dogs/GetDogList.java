package com.k9x.infrastructure.in.rest.endpoints.secured.dogs;

import com.k9x.application.dogs.dto.DogDTO;
import com.k9x.application.dogs.use_case.GetDogListServiceCase;
import com.k9x.application.users.dto.AuthTokenDTO;
import com.k9x.oas.stub.api.SecuredDogsFetchAllApiDelegate;
import com.k9x.oas.stub.model.DogSummaryResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class GetDogList implements SecuredDogsFetchAllApiDelegate {

    private final GetDogListServiceCase getDogListService;
    private final AuthTokenDTO userDetails;

    public GetDogList(GetDogListServiceCase getDogListService, AuthTokenDTO userDetails) {
        this.getDogListService = getDogListService;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<List<DogSummaryResponseDTO>> getDogsSecured() {
        List<DogDTO> dogs = getDogListService.getDogs(userDetails.getSubject());
        List<DogSummaryResponseDTO> mapped = dogs.stream()
                .map(dog -> new DogSummaryResponseDTO(dog.id(), dog.name(), dog.image(), true, dog.owner(), null, null, dog.owner(), null))
                .toList();
        return ResponseEntity.ok(mapped);
    }
}
