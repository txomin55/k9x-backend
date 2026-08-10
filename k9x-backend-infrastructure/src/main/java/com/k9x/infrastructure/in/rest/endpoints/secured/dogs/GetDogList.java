package com.k9x.infrastructure.in.rest.endpoints.secured.dogs;

import com.k9x.application.dogs.use_case.GetDogListServiceCase;
import com.k9x.application.dogs.use_case.dto.DogDTO;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import com.k9x.oas.stub.api.SecuredDogsFetchAllApiDelegate;
import com.k9x.oas.stub.model.DogSummaryResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class GetDogList implements SecuredDogsFetchAllApiDelegate {

    private final GetDogListServiceCase getDogListService;
    private final UserInfoDTO userDetails;
    private final ReferenceNameResolver referenceNames;

    public GetDogList(GetDogListServiceCase getDogListService, UserInfoDTO userDetails,
                      ReferenceNameResolver referenceNames) {
        this.getDogListService = getDogListService;
        this.userDetails = userDetails;
        this.referenceNames = referenceNames;
    }

    @Override
    public ResponseEntity<List<DogSummaryResponseDTO>> getDogsSecured(Boolean owned, Boolean created) {
        List<DogDTO> dogs = getDogListService.getDogs(
                userDetails.getEmail(),
                userDetails.isOrganizer(),
                owned != null && owned,
                created != null && created);
        List<DogSummaryResponseDTO> mapped = dogs.stream()
                .map(dog ->
                        new DogSummaryResponseDTO(
                                dog.identification(),
                                dog.name(),
                                dog.image(),
                                dog.owned(),
                                referenceNames.country(dog.country()),
                                dog.team(),
                                dog.owner(),
                                dog.handler(),
                                dog.origin(),
                                dog.license(),
                                referenceNames.breed(dog.breed()),
                                dog.sex() == null ? null : dog.sex().name(),
                                dog.withersCm(),
                                dog.threeFciGenerationsConfirmed()
                        )
                )
                .toList();
        return ResponseEntity.ok(mapped);
    }
}
