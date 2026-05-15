package com.k9x.infrastructure.in.rest.endpoints.secured.competitions;

import com.k9x.oas.stub.api.SecuredCompetitionsCreateApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.http.ResponseEntity;

public class CreateCompetition implements SecuredCompetitionsCreateApiDelegate {

    @Override
    public ResponseEntity<String> createCompetitionSecured(IdNameDTO body) {
        return ResponseEntity.ok("MOCKED");
    }
}
