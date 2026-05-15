package com.k9x.infrastructure.in.rest.endpoints.secured.judges;

import com.k9x.oas.stub.api.SecuredJudgesUpdateApiDelegate;
import com.k9x.oas.stub.model.UpdateJudgeRequestDTO;
import org.springframework.http.ResponseEntity;

public class UpdateJudge implements SecuredJudgesUpdateApiDelegate {

    @Override
    public ResponseEntity<String> updateJudgeSecured(String id, UpdateJudgeRequestDTO body) {
        return ResponseEntity.ok("MOCKED");
    }
}
