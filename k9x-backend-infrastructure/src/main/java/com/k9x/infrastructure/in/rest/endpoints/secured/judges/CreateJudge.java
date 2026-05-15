package com.k9x.infrastructure.in.rest.endpoints.secured.judges;

import com.k9x.oas.stub.api.SecuredJudgesCreateApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.http.ResponseEntity;

public class CreateJudge implements SecuredJudgesCreateApiDelegate {

    @Override
    public ResponseEntity<String> createJudgeSecured(IdNameDTO body) {
        return ResponseEntity.ok("MOCKED");
    }
}
