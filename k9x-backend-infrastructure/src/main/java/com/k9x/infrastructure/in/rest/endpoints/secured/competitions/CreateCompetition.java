package com.k9x.infrastructure.in.rest.endpoints.secured.competitions;

import com.k9x.application.competitions.use_case.CreateCompetitionServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredCompetitionsCreateApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.http.ResponseEntity;

public class CreateCompetition implements SecuredCompetitionsCreateApiDelegate {

    private final CreateCompetitionServiceCase createCompetitionServiceCase;
    private final UserInfoDTO userDetails;

    public CreateCompetition(CreateCompetitionServiceCase createCompetitionServiceCase, UserInfoDTO userDetails) {
        this.createCompetitionServiceCase = createCompetitionServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> createCompetitionSecured(IdNameDTO body) {
        createCompetitionServiceCase.createCompetition(body.getId(), body.getName(), userDetails.getEmail(), userDetails.isOrganizer());
        return ResponseEntity.ok().build();
    }
}
