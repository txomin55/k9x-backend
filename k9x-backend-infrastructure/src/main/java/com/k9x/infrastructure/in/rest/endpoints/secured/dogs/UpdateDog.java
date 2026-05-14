package com.k9x.infrastructure.in.rest.endpoints.secured.dogs;

import com.k9x.oas.stub.api.SecuredDogsUpdateApiDelegate;
import com.k9x.oas.stub.model.UpdateDogRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UpdateDog implements SecuredDogsUpdateApiDelegate {

    @Override
    public ResponseEntity<String> updateDogSecured(String id, UpdateDogRequestDTO body) {
        return ResponseEntity.ok("MOCKED");
    }

}
