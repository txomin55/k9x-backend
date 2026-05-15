package com.k9x.infrastructure.in.rest.endpoints.secured.competitions;

import com.k9x.oas.stub.api.SecuredCompetitionsUpdateApiDelegate;
import com.k9x.oas.stub.model.UpdateCompetitionRequestDTO;
import org.springframework.http.ResponseEntity;

public class UpdateCompetition implements SecuredCompetitionsUpdateApiDelegate {

    @Override
    public ResponseEntity<String> updateCompetitionSecured(String id, UpdateCompetitionRequestDTO body) {
        return ResponseEntity.ok("MOCKED");
    }
}
