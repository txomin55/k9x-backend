package com.k9x.infrastructure.in.rest.endpoints.secured.judges;

import com.k9x.oas.stub.api.SecuredJudgesRemoveApiDelegate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class RemoveJudge implements SecuredJudgesRemoveApiDelegate {

    @Override
    public ResponseEntity<String> removeJudgeSecured(String id) {
        return ResponseEntity.ok("MOCKED");
    }
}
