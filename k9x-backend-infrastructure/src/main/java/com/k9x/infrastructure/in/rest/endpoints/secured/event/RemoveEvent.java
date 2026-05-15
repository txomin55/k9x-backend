package com.k9x.infrastructure.in.rest.endpoints.secured.event;

import com.k9x.oas.stub.api.SecuredEventsDeleteApiDelegate;
import org.springframework.http.ResponseEntity;

public class RemoveEvent implements SecuredEventsDeleteApiDelegate {

    @Override
    public ResponseEntity<String> removeEventSecured(String id) {
        return ResponseEntity.ok("MOCKED");
    }
}
