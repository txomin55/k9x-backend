package com.k9x.infrastructure.in.rest.endpoints.dogs;

import com.k9x.application.dogs.use_case.GetPublicDogListServiceCase;
import com.k9x.application.dogs.use_case.command.GetPublicDogListCommand;
import com.k9x.application.dogs.use_case.dto.PublicDogListDTO;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import com.k9x.oas.stub.api.DogsFetchAllApiDelegate;
import com.k9x.oas.stub.model.PublicDogListResponseDTO;
import com.k9x.oas.stub.model.PublicDogSummaryResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Public dog directory. Carries no {@code UserInfoDTO}: the path is outside {@code /secured/}, so the auth
 * filter lets the request through and there is no user on it to inject.
 */
public class FetchAllDogs implements DogsFetchAllApiDelegate {

    private final GetPublicDogListServiceCase getPublicDogListServiceCase;
    private final ReferenceNameResolver referenceNames;

    public FetchAllDogs(GetPublicDogListServiceCase getPublicDogListServiceCase,
                        ReferenceNameResolver referenceNames) {
        this.getPublicDogListServiceCase = getPublicDogListServiceCase;
        this.referenceNames = referenceNames;
    }

    @Override
    public ResponseEntity<PublicDogListResponseDTO> fetchAllDogs(String name, String handler, String country,
                                                                 Integer page, Integer size) {
        PublicDogListDTO dogs = getPublicDogListServiceCase.getDogs(
                new GetPublicDogListCommand(name, handler, country, page, size));
        List<PublicDogSummaryResponseDTO> mapped = dogs.items().stream()
                .map(dog -> new PublicDogSummaryResponseDTO(
                        dog.identification(),
                        dog.name(),
                        dog.handler(),
                        referenceNames.country(dog.country()),
                        dog.sex() == null ? null : dog.sex().name(),
                        referenceNames.breed(dog.breed()),
                        dog.rank()))
                .toList();
        return ResponseEntity.ok(
                new PublicDogListResponseDTO(mapped, dogs.page(), dogs.size(), dogs.total(), dogs.totalPages()));
    }
}
