package com.k9x.infrastructure.in.rest.endpoints.secured.dogs;

import com.k9x.oas.stub.api.SecuredDogsRemoveApiDelegate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class RemoveDog implements SecuredDogsRemoveApiDelegate {

    @Override
    public ResponseEntity<String> deleteDogSecured(String id) {
        return ResponseEntity.ok("MOCKED");
    }
}
