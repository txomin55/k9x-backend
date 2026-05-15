package com.k9x.infrastructure.in.rest.endpoints.secured.stages;

import com.k9x.oas.stub.api.SecuredStagesRemoveApiDelegate;
import org.springframework.http.ResponseEntity;

public class RemoveStage implements SecuredStagesRemoveApiDelegate {

    @Override
    public ResponseEntity<String> removeStage(String id) {
        return ResponseEntity.ok("MOCKED");
    }
}
