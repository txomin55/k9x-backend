package com.k9x.infrastructure.in.rest.endpoints.secured.collections.obdx;

import com.k9x.application.collections.obdx.use_case.RegisterObdxRedCardServiceCase;
import com.k9x.application.collections.obdx.use_case.command.RegisterObdxRedCardCommand;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredEventsUpdateRedCardObdxApiDelegate;
import com.k9x.oas.stub.model.RegisterRedCardRequestDTO;
import org.springframework.http.ResponseEntity;

public class RegisterObdxRedCard implements SecuredEventsUpdateRedCardObdxApiDelegate {

    private final RegisterObdxRedCardServiceCase registerObdxRedCardServiceCase;
    private final UserInfoDTO userDetails;

    public RegisterObdxRedCard(RegisterObdxRedCardServiceCase registerObdxRedCardServiceCase, UserInfoDTO userDetails) {
        this.registerObdxRedCardServiceCase = registerObdxRedCardServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> registerRedCard(String eventId, RegisterRedCardRequestDTO body) {
        registerObdxRedCardServiceCase.registerRedCard(
                eventId,
                new RegisterObdxRedCardCommand(body.getJudgeId(), body.getExerciseId(), body.getDogIdentification()),
                userDetails.getEmail());
        return ResponseEntity.ok().build();
    }
}
