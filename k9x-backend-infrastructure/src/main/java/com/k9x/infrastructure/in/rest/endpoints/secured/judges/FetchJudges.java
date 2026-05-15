package com.k9x.infrastructure.in.rest.endpoints.secured.judges;

import com.k9x.oas.stub.api.SecuredJudgesFetchAllApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class FetchJudges implements SecuredJudgesFetchAllApiDelegate {

    @Override
    public ResponseEntity<List<IdNameDTO>> fetchJudgesSecured() {
        return ResponseEntity.ok(List.of(new IdNameDTO("Judge One", "judge-1")));
    }
}
