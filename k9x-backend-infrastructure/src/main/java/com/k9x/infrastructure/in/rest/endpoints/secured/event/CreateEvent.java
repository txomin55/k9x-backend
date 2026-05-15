package com.k9x.infrastructure.in.rest.endpoints.secured.event;

import com.k9x.oas.stub.api.SecuredEventsCreateApiDelegate;
import com.k9x.oas.stub.model.CreateEventRequestDTO;
import org.springframework.http.ResponseEntity;

public class CreateEvent implements SecuredEventsCreateApiDelegate {

    @Override
    public ResponseEntity<String> createEventSecured(CreateEventRequestDTO body) {
        return ResponseEntity.ok("MOCKED");
    }
}
