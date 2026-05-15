package com.k9x.infrastructure.in.rest.endpoints.secured.event;

import com.k9x.oas.stub.api.SecuredEventsEnrollApiDelegate;
import com.k9x.oas.stub.model.EnrollStageEventRequestDTO;
import org.springframework.http.ResponseEntity;

public class EnrollEvent implements SecuredEventsEnrollApiDelegate {

    public ResponseEntity<Object> doEnrollSecured(String stageId, String eventId, EnrollStageEventRequestDTO body) {
        return ResponseEntity.ok("MOCKED");
    }
}
