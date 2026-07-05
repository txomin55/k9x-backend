package com.k9x.infrastructure.in.rest.endpoints.secured.dogs;

import com.k9x.application.dogs.use_case.GetDogListServiceCase;
import com.k9x.application.dogs.use_case.dto.DogDTO;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredDogsFetchAllApiDelegate;
import com.k9x.oas.stub.model.DogSummaryResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class GetDogList implements SecuredDogsFetchAllApiDelegate {

    private final GetDogListServiceCase getDogListService;
    private final UserInfoDTO userDetails;

    public GetDogList(GetDogListServiceCase getDogListService, UserInfoDTO userDetails) {
        this.getDogListService = getDogListService;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<List<DogSummaryResponseDTO>> getDogsSecured(Boolean onlyOwned) {
        List<DogDTO> dogs = getDogListService.getDogs(userDetails.getEmail(), userDetails.isOrganizer(), onlyOwned != null ? onlyOwned : false);
        List<DogSummaryResponseDTO> mapped = dogs.stream()
                .map(dog ->
                        new DogSummaryResponseDTO(
                                dog.id(),
                                dog.name(),
                                dog.image(),
                                dog.owned(),
                                dog.country(),
                                dog.team(),
                                dog.owner(),
                                dog.handler(),
                                dog.identity(),
                                dog.breed(),
                                dog.sex() == null ? null : dog.sex().name(),
                                dog.withersCm(),
                                dog.threeFciGenerationsConfirmed()
                        )
                )
                .toList();
        return ResponseEntity.ok(mapped);
    }
}
