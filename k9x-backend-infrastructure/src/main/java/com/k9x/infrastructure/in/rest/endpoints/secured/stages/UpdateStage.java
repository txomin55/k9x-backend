package com.k9x.infrastructure.in.rest.endpoints.secured.stages;

import com.k9x.oas.stub.api.SecuredStagesUpdateApiDelegate;
import com.k9x.oas.stub.model.UpdateStageRequestDTO;
import org.springframework.http.ResponseEntity;

public class UpdateStage implements SecuredStagesUpdateApiDelegate {

    @Override
    public ResponseEntity<String> updateStage(String id, UpdateStageRequestDTO body) {
        return ResponseEntity.ok("MOCKED");
    }
}
