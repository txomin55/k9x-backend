package com.k9x.infrastructure.in.rest.endpoints.secured.competitions;

import com.k9x.application.competitions.command.UpdateCompetitionCommand;
import com.k9x.application.competitions.use_case.UpdateCompetitionServiceCase;
import com.k9x.application.users.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredCompetitionsUpdateApiDelegate;
import com.k9x.oas.stub.model.UpdateCompetitionRequestDTO;
import org.springframework.http.ResponseEntity;

public class UpdateCompetition implements SecuredCompetitionsUpdateApiDelegate {

    private final UpdateCompetitionServiceCase updateCompetitionServiceCase;
    private final UserInfoDTO userDetails;

    public UpdateCompetition(UpdateCompetitionServiceCase updateCompetitionServiceCase, UserInfoDTO userDetails) {
        this.updateCompetitionServiceCase = updateCompetitionServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> updateCompetitionSecured(String id, UpdateCompetitionRequestDTO body) {
        updateCompetitionServiceCase.updateCompetition(id,
                new UpdateCompetitionCommand(body.getName(), body.getDescription(), body.getCountry(), body.getAddress()),
                userDetails.getEmail(), userDetails.isOrganizer());
        return ResponseEntity.ok().build();
    }
}
