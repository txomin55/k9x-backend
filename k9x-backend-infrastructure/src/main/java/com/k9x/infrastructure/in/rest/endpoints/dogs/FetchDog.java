package com.k9x.infrastructure.in.rest.endpoints.dogs;

import com.k9x.application.dogs.use_case.GetPublicDogServiceCase;
import com.k9x.application.dogs.use_case.dto.PublicDogDetailDTO;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import com.k9x.oas.stub.api.DogsFetchOneApiDelegate;
import com.k9x.oas.stub.model.PublicDogDetailResponseDTO;
import org.springframework.http.ResponseEntity;

/**
 * Public dog detail. Carries no {@code UserInfoDTO}: the path is outside {@code /secured/}, so the auth
 * filter lets the request through and there is no user on it to inject.
 */
public class FetchDog implements DogsFetchOneApiDelegate {

    private final GetPublicDogServiceCase getPublicDogServiceCase;
    private final ReferenceNameResolver referenceNames;

    public FetchDog(GetPublicDogServiceCase getPublicDogServiceCase, ReferenceNameResolver referenceNames) {
        this.getPublicDogServiceCase = getPublicDogServiceCase;
        this.referenceNames = referenceNames;
    }

    @Override
    public ResponseEntity<PublicDogDetailResponseDTO> fetchDog(String identification) {
        PublicDogDetailDTO dog = getPublicDogServiceCase.getDog(identification);
        return ResponseEntity.ok(new PublicDogDetailResponseDTO(
                dog.identification(),
                dog.name(),
                dog.image(),
                referenceNames.breed(dog.breed()),
                dog.origin(),
                dog.license(),
                referenceNames.country(dog.country()),
                dog.team(),
                dog.handler(),
                dog.sex() == null ? null : dog.sex().name(),
                dog.withersCm(),
                dog.threeFciGenerationsConfirmed(),
                dog.lastUpdate()));
    }
}
