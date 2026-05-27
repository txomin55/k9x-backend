package com.k9x.infrastructure.in.rest.endpoints.secured.events.obdx;

import com.k9x.oas.stub.api.SecuredEventsUpdateScoreObdxApiDelegate;
import com.k9x.oas.stub.model.UpdateCollectionScoreRequestDTO;
import org.springframework.http.ResponseEntity;

public class UpdateObdxScore implements SecuredEventsUpdateScoreObdxApiDelegate {

    @Override
    public ResponseEntity<String> updateObdxScore(String eventId, UpdateCollectionScoreRequestDTO body) {
        return ResponseEntity.ok("MOCKED");
    }
}
