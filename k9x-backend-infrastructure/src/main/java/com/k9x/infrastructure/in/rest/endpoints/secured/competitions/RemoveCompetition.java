package com.k9x.infrastructure.in.rest.endpoints.secured.competitions;

import com.k9x.application.competitions.use_case.DeleteCompetitionServiceCase;
import com.k9x.application.users.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredCompetitionsRemoveApiDelegate;
import org.springframework.http.ResponseEntity;

public class RemoveCompetition implements SecuredCompetitionsRemoveApiDelegate {

    private final DeleteCompetitionServiceCase deleteCompetitionServiceCase;
    private final UserInfoDTO userDetails;

    public RemoveCompetition(DeleteCompetitionServiceCase deleteCompetitionServiceCase, UserInfoDTO userDetails) {
        this.deleteCompetitionServiceCase = deleteCompetitionServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> removeCompetitionSecured(String id) {
        deleteCompetitionServiceCase.deleteCompetition(id, userDetails.getEmail(), userDetails.isOrganizer());
        return ResponseEntity.ok().build();
    }
}
