package com.k9x.infrastructure.in.rest.endpoints.secured.dogs;

import com.k9x.application.dogs.use_case.DeleteDogServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredDogsRemoveApiDelegate;
import org.springframework.http.ResponseEntity;

public class RemoveDog implements SecuredDogsRemoveApiDelegate {

    private final DeleteDogServiceCase deleteDogServiceCase;
    private final UserInfoDTO userDetails;

    public RemoveDog(DeleteDogServiceCase deleteDogServiceCase, UserInfoDTO userDetails) {
        this.deleteDogServiceCase = deleteDogServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> deleteDogSecured(String identification) {
        deleteDogServiceCase.deleteDog(identification, userDetails.getEmail(), userDetails.isOrganizer());
        return ResponseEntity.ok().build();
    }
}
