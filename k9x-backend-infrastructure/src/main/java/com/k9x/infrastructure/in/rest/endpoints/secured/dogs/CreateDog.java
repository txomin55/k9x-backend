package com.k9x.infrastructure.in.rest.endpoints.secured.dogs;

import com.k9x.application.dogs.use_case.CreateDogServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredDogsCreateApiDelegate;
import com.k9x.oas.stub.model.CreateDogRequestDTO;
import org.springframework.http.ResponseEntity;

public class CreateDog implements SecuredDogsCreateApiDelegate {

    private final CreateDogServiceCase createDogServiceCase;
    private final UserInfoDTO userDetails;

    public CreateDog(CreateDogServiceCase createDogServiceCase, UserInfoDTO userDetails) {
        this.createDogServiceCase = createDogServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> createDogSecured(CreateDogRequestDTO body) {
        createDogServiceCase.createDog(
                body.getId(),
                body.getName(),
                body.getImage(),
                body.getBreed(),
                body.getIdentifier(),
                body.getOwner(),
                body.getHandler(),
                userDetails.getEmail(),
                body.getTeam(),
                body.getCountry(),
                userDetails.isOrganizer()
        );
        return ResponseEntity.ok().build();
    }
}
