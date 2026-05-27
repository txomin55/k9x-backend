package com.k9x.infrastructure.in.rest.endpoints.secured.event.obdx;

import com.k9x.oas.stub.api.SecuredEventsUpdateInfoObdxApiDelegate;
import com.k9x.oas.stub.model.UpdateEventRequestDTO;
import org.springframework.http.ResponseEntity;

public class UpdateObdxEventInfo implements SecuredEventsUpdateInfoObdxApiDelegate {

    @Override
    public ResponseEntity<String> updateObdxEventSecured(String id, UpdateEventRequestDTO body) {
        return ResponseEntity.ok("MOCKED");
    }
}

/*
* {
    "name": "string",
    "competitors": [
        {
            "dogId": "string",
            "owner": "string",
            "identity": "string",
            "team": "string",
            "country": "string",
            "order": 0,
            "status": "string"
        }
    ],
    "exercises": [
        {
            "id": "string",
            "name": "string",
            "order": 0,
            "tags": [
                "string"
            ]
        }
    ],
    "configurationId": "string",
    "judges": [
        {
            "id": "string",
            "collectorEmail": "string"
        }
    ]
}
* */