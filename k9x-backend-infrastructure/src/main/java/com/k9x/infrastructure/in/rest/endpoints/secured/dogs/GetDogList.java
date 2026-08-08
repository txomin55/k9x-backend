package com.k9x.infrastructure.in.rest.endpoints.secured.dogs;

import com.k9x.application.dogs.use_case.GetDogListServiceCase;
import com.k9x.application.dogs.use_case.dto.DogDTO;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredDogsFetchAllApiDelegate;
import com.k9x.oas.stub.model.DogSummaryResponseDTO;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class GetDogList implements SecuredDogsFetchAllApiDelegate {

    private final GetDogListServiceCase getDogListService;
    private final UserInfoDTO userDetails;
    private final MessageSource messageSource;

    public GetDogList(GetDogListServiceCase getDogListService, UserInfoDTO userDetails, MessageSource messageSource) {
        this.getDogListService = getDogListService;
        this.userDetails = userDetails;
        this.messageSource = messageSource;
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
                                resolveCountry(dog.country()),
                                dog.team(),
                                dog.owner(),
                                dog.handler(),
                                dog.origin(),
                                dog.license(),
                                resolveBreed(dog.breed()),
                                dog.sex() == null ? null : dog.sex().name(),
                                dog.withersCm(),
                                dog.threeFciGenerationsConfirmed()
                        )
                )
                .toList();
        return ResponseEntity.ok(mapped);
    }

    private IdNameDTO resolveCountry(String countryCode) {
        if (countryCode == null) {
            return null;
        }
        String name = messageSource.getMessage(
                "country." + countryCode.toLowerCase() + ".name", null, countryCode, LocaleContextHolder.getLocale());
        return new IdNameDTO(name, countryCode);
    }

    private IdNameDTO resolveBreed(String breedId) {
        if (breedId == null) {
            return null;
        }
        String name = messageSource.getMessage(
                "breed." + breedId.toLowerCase() + ".name", null, breedId, LocaleContextHolder.getLocale());
        return new IdNameDTO(name, breedId);
    }
}
