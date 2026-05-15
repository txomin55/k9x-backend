package com.k9x.infrastructure.in.rest.endpoints.secured.event;

import com.k9x.oas.stub.api.SecuredEventsUpdateInfoObdxApiDelegate;
import com.k9x.oas.stub.model.UpdateEventRequestDTO;
import org.springframework.http.ResponseEntity;

public class UpdateObdxEventInfo implements SecuredEventsUpdateInfoObdxApiDelegate {

    @Override
    public ResponseEntity<String> updateObdxEventSecured(String id, UpdateEventRequestDTO body) {
        return ResponseEntity.ok("MOCKED");
    }
}
