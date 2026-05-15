package com.k9x.infrastructure.in.rest.endpoints.secured.dogs;

import com.k9x.oas.stub.api.SecuredDogsCreateApiDelegate;
import com.k9x.oas.stub.model.CreateDogRequestDTO;
import org.springframework.http.ResponseEntity;

public class CreateDog implements SecuredDogsCreateApiDelegate {

    @Override
    public ResponseEntity<String> createDogSecured(CreateDogRequestDTO body) {
        return ResponseEntity.ok("MOCKED");
    }

}
