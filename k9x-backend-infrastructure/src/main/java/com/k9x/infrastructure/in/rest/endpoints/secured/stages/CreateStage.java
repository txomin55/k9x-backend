package com.k9x.infrastructure.in.rest.endpoints.secured.stages;

import com.k9x.oas.stub.api.SecuredStagesCreateApiDelegate;
import com.k9x.oas.stub.model.CreateStageRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CreateStage implements SecuredStagesCreateApiDelegate {

    @Override
    public ResponseEntity<String> createStage(CreateStageRequestDTO body) {
        return ResponseEntity.ok("MOCKED");
    }
}
