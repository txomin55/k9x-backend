package com.k9x.infrastructure.in.rest.endpoints.secured.dogs;

import com.k9x.application.dogs.use_case.command.UpdateDogCommand;
import com.k9x.application.dogs.use_case.UpdateDogServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredDogsUpdateApiDelegate;
import com.k9x.oas.stub.model.UpdateDogRequestDTO;
import org.springframework.http.ResponseEntity;

public class UpdateDog implements SecuredDogsUpdateApiDelegate {

    private final UpdateDogServiceCase updateDogServiceCase;
    private final UserInfoDTO userDetails;

    public UpdateDog(UpdateDogServiceCase updateDogServiceCase, UserInfoDTO userDetails) {
        this.updateDogServiceCase = updateDogServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> updateDogSecured(String id, UpdateDogRequestDTO body) {
        updateDogServiceCase.updateDog(id,
                new UpdateDogCommand(body.getName(), body.getImage(), body.getBreed(), body.getIdentity(),
                        body.getOwner(), body.getHandler(), body.getTeam(), body.getCountry()),
                userDetails.getEmail(), userDetails.isOrganizer());
        return ResponseEntity.ok().build();
    }
}
