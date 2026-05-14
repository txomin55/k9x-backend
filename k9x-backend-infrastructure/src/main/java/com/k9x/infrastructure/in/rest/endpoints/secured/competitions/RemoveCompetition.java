package com.k9x.infrastructure.in.rest.endpoints.secured.competitions;

import com.k9x.oas.stub.api.SecuredCompetitionsRemoveApiDelegate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class RemoveCompetition implements SecuredCompetitionsRemoveApiDelegate {

    @Override
    public ResponseEntity<String> removeCompetitionSecured(String id) {
        return ResponseEntity.ok("MOCKED");
    }
}
